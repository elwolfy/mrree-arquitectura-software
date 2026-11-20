package pe.utec.consular.pagos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.pagos.adapter.in.web.AnularPagoRequest;
import pe.utec.consular.pagos.adapter.in.web.PagoDto;
import pe.utec.consular.pagos.adapter.in.web.RegistrarPagoRequest;
import pe.utec.consular.pagos.adapter.out.outbox.RabbitConfig;
import pe.utec.consular.pagos.adapter.out.persistence.SpringDataPagoRepository;
import pe.utec.consular.pagos.domain.EstadoPago;
import pe.utec.consular.pagos.domain.PagoAnulado;
import pe.utec.consular.pagos.domain.PagoConfirmado;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integracion")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "outbox.relay.intervalo-ms=150"
)
@DisplayName("Pruebas de integración de pagos-service en M3 (Idempotency-Key, Transactional Outbox y Anulación)")
class PagosIntegracionTest {

    private static final String COLA_PAGO_CONFIRMADO = "test.pagos.confirmado";
    private static final String COLA_PAGO_ANULADO = "test.pagos.anulado";

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
        registry.add("spring.datasource.username", () -> "pagos_app");
        registry.add("spring.datasource.password", () -> "pagos_pwd");
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataPagoRepository pagoRepository;

    private final RestTemplate restTemplate = createRestTemplate();

    private static RestTemplate createRestTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(HttpStatusCode statusCode) {
                return false;
            }
        });
        return rt;
    }

    @BeforeEach
    void prepararRabbit() {
        TopicExchange exchange = new TopicExchange(RabbitConfig.EXCHANGE_EVENTOS, true, false);
        Queue qConfirmado = new Queue(COLA_PAGO_CONFIRMADO, true);
        Queue qAnulado = new Queue(COLA_PAGO_ANULADO, true);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(qConfirmado);
        amqpAdmin.declareQueue(qAnulado);
        amqpAdmin.declareBinding(BindingBuilder.bind(qConfirmado).to(exchange).with("pago.confirmado"));
        amqpAdmin.declareBinding(BindingBuilder.bind(qAnulado).to(exchange).with("pago.anulado"));
        amqpAdmin.purgeQueue(COLA_PAGO_CONFIRMADO, false);
        amqpAdmin.purgeQueue(COLA_PAGO_ANULADO, false);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("POST /api/pagos sin Idempotency-Key devuelve 400 ProblemDetail")
    void postSinIdempotencyKeyDevuelve400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RegistrarPagoRequest request = new RegistrarPagoRequest(UUID.randomUUID(), new BigDecimal("33.00"));

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/pagos"), new HttpEntity<>(request, headers), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("validacion", response.getBody().get("codigo"));
    }

    @Test
    @DisplayName("Dos POST /api/pagos con la misma Idempotency-Key crean un único registro, devuelven la misma respuesta 200 y publican un solo pago.confirmado")
    void dosPostConMismaClaveSonIdempotentes() throws Exception {
        UUID tramiteId = UUID.randomUUID();
        String idempotencyKey = "idem-" + UUID.randomUUID();
        RegistrarPagoRequest request = new RegistrarPagoRequest(tramiteId, new BigDecimal("33.00"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);

        // Primer POST
        ResponseEntity<PagoDto> resp1 = restTemplate.postForEntity(
                url("/api/pagos"), new HttpEntity<>(request, headers), PagoDto.class);
        assertEquals(HttpStatus.OK, resp1.getStatusCode());
        assertNotNull(resp1.getBody());
        assertEquals(EstadoPago.CONFIRMADO, resp1.getBody().estado());

        // Segundo POST con la misma Idempotency-Key
        ResponseEntity<PagoDto> resp2 = restTemplate.postForEntity(
                url("/api/pagos"), new HttpEntity<>(request, headers), PagoDto.class);
        assertEquals(HttpStatus.OK, resp2.getStatusCode());
        assertNotNull(resp2.getBody());

        // Ambas respuestas representan exactamente el mismo pago
        assertEquals(resp1.getBody().id(), resp2.getBody().id());
        assertEquals(resp1.getBody().tramiteId(), resp2.getBody().tramiteId());
        assertEquals(resp1.getBody().monto(), resp2.getBody().monto());
        assertEquals(resp1.getBody().claveIdempotencia(), resp2.getBody().claveIdempotencia());

        // Solo 1 registro en pagos.pago para ese trámite
        assertEquals(1, pagoRepository.findByTramiteIdOrderByCreadoEnDesc(tramiteId).size());

        // Solo 1 evento pago.confirmado publicado en RabbitMQ vía Outbox
        Message msg1 = rabbitTemplate.receive(COLA_PAGO_CONFIRMADO, 3000);
        assertNotNull(msg1, "Debe publicarse pago.confirmado vía Outbox");
        PagoConfirmado evento = objectMapper.readValue(msg1.getBody(), PagoConfirmado.class);
        assertEquals(resp1.getBody().id(), evento.pagoId());
        assertEquals(tramiteId, evento.tramiteId());

        Message msg2 = rabbitTemplate.receive(COLA_PAGO_CONFIRMADO, 400);
        assertNull(msg2, "El segundo POST idempotente no debe publicar otro evento pago.confirmado");
    }

    @Test
    @DisplayName("POST /api/pagos/{id}/anulacion cambia el pago a ANULADO y publica pago.anulado vía Outbox")
    void anularPagoCambiaEstadoYPublicaEvento() throws Exception {
        UUID tramiteId = UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", "idem-anular-" + UUID.randomUUID());

        ResponseEntity<PagoDto> creado = restTemplate.postForEntity(
                url("/api/pagos"),
                new HttpEntity<>(new RegistrarPagoRequest(tramiteId, new BigDecimal("22.00")), headers),
                PagoDto.class
        );
        assertEquals(HttpStatus.OK, creado.getStatusCode());
        assertNotNull(creado.getBody());

        HttpHeaders headersAnular = new HttpHeaders();
        headersAnular.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<PagoDto> anulado = restTemplate.postForEntity(
                url("/api/pagos/" + creado.getBody().id() + "/anulacion"),
                new HttpEntity<>(new AnularPagoRequest("Falla en servicio de emisión"), headersAnular),
                PagoDto.class
        );

        assertEquals(HttpStatus.OK, anulado.getStatusCode());
        assertNotNull(anulado.getBody());
        assertEquals(EstadoPago.ANULADO, anulado.getBody().estado());

        Message msgAnulado = rabbitTemplate.receive(COLA_PAGO_ANULADO, 3000);
        assertNotNull(msgAnulado, "Debe publicarse pago.anulado vía Outbox");
        PagoAnulado evento = objectMapper.readValue(msgAnulado.getBody(), PagoAnulado.class);
        assertEquals(creado.getBody().id(), evento.pagoId());
        assertEquals(tramiteId, evento.tramiteId());
        assertEquals("Falla en servicio de emisión", evento.motivo());
    }
}

