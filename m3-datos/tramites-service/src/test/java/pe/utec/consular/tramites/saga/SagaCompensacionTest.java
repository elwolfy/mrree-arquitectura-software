package pe.utec.consular.tramites.saga;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.tramites.adapter.in.web.RegistrarTramiteRequest;
import pe.utec.consular.tramites.adapter.in.web.TramiteDto;
import pe.utec.consular.tramites.adapter.out.http.AnulacionPagoHttpAdapter;
import pe.utec.consular.tramites.application.saga.SagaSolicitudTramite;
import pe.utec.consular.tramites.domain.EmisionFallida;
import pe.utec.consular.tramites.domain.EstadoTramite;
import pe.utec.consular.tramites.domain.PagoConfirmado;
import pe.utec.consular.tramites.domain.TipoTramite;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Asignación A3")
@Tag("integracion")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "grpc.port=0",
                "outbox.relay.intervalo-ms=150"
        }
)
@DisplayName("Asignación A3 — Compensación de la Saga ante falla de emisión (Anulación de pago + RECHAZADO)")
class SagaCompensacionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("consular")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("01-esquemas.sql");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "tramites_app");
        registry.add("spring.datasource.password", () -> "tramites_pwd");
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @LocalServerPort
    private int httpPort;

    @Autowired
    private SagaSolicitudTramite sagaSolicitudTramite;

    @Autowired
    private AnulacionPagoHttpAdapter anulacionPagoHttpAdapter;

    private HttpServer mockPagosServer;
    private final AtomicReference<String> ultimoPathAnulacion = new AtomicReference<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void iniciarMockPagos() throws Exception {
        ultimoPathAnulacion.set(null);
        mockPagosServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockPagosServer.createContext("/api/pagos", exchange -> {
            ultimoPathAnulacion.set(exchange.getRequestURI().getPath());
            byte[] resp = "{\"estado\":\"ANULADO\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        mockPagosServer.start();
        int mockPort = mockPagosServer.getAddress().getPort();
        anulacionPagoHttpAdapter.setPagosServiceUrl("http://localhost:" + mockPort);
    }

    @AfterEach
    void detenerMockPagos() {
        if (mockPagosServer != null) {
            mockPagosServer.stop(0);
        }
    }

    @Test
    @DisplayName("Ante emision.fallida, la saga solicita la anulación del pago y pasa el trámite a RECHAZADO con motivo_rechazo")
    void alFallarEmisionCompensaAnulandoPagoYRechazandoTramite() {
        // 1. Registrar trámite (PENDIENTE_PAGO)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RegistrarTramiteRequest req = new RegistrarTramiteRequest(
                "40000001", "Ana Pérez", "ana@correo.lab",
                TipoTramite.APOSTILLA, "ES", 2, "ES"
        );
        ResponseEntity<TramiteDto> respRegistro = restTemplate.postForEntity(
                "http://localhost:" + httpPort + "/api/tramites",
                new HttpEntity<>(req, headers),
                TramiteDto.class
        );
        assertEquals(HttpStatus.CREATED, respRegistro.getStatusCode());
        TramiteDto tramite = respRegistro.getBody();
        assertNotNull(tramite);

        // 2. Confirmar pago -> trámite pasa a EN_EMISION
        UUID pagoId = UUID.randomUUID();
        sagaSolicitudTramite.alConfirmarPago(new PagoConfirmado(pagoId, tramite.id(), tramite.monto()));

        // 3. Simular recepción de emision.fallida ( dos veces para validar idempotencia de la compensación )
        String motivoFalla = "Falla simulada en el servicio de firma digital consular";
        sagaSolicitudTramite.alFallarEmision(new EmisionFallida(tramite.id(), motivoFalla));
        sagaSolicitudTramite.alFallarEmision(new EmisionFallida(tramite.id(), motivoFalla));

        // 4. Verificar que se invocó POST /api/pagos/{pagoId}/anulacion
        assertEquals("/api/pagos/" + pagoId + "/anulacion", ultimoPathAnulacion.get(),
                "La compensación debe solicitar la anulación del pago a pagos-service");

        // 5. Verificar que la vista CQRS refleja estado RECHAZADO y motivoRechazo
        ResponseEntity<TramiteDto> respConsulta = restTemplate.getForEntity(
                "http://localhost:" + httpPort + "/api/tramites/" + tramite.id(),
                TramiteDto.class
        );
        assertEquals(HttpStatus.OK, respConsulta.getStatusCode());
        assertNotNull(respConsulta.getBody());
        assertEquals(EstadoTramite.RECHAZADO, respConsulta.getBody().estado());
        assertEquals(motivoFalla, respConsulta.getBody().motivoRechazo());
    }
}

