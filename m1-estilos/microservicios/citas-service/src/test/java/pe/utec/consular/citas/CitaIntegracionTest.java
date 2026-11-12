package pe.utec.consular.citas;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.citas.adapter.in.web.CitaDto;
import pe.utec.consular.citas.adapter.in.web.InfoSistemaDto;
import pe.utec.consular.citas.adapter.in.web.RegistrarCitaRequest;
import pe.utec.consular.citas.domain.TipoTramite;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración de {@code citas-service} con Testcontainers PostgreSQL 16 y WireMock.
 * <p>Concepto: Verificación del Circuit Breaker Resilience4j y degradación elegante cuando
 * {@code notificaciones-service} se encuentra caído (secciones 1.2 y 4.3 del contrato global).
 * <p>Sesión: S1.
 */
@Tag("integracion")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas de integración de citas-service con Testcontainers y WireMock")
class CitaIntegracionTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("consular")
            .withUsername("test")
            .withPassword("test");

    private static final WireMockServer wireMockServer;

    static {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void detenerWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> postgres.getJdbcUrl() + "&currentSchema=citas");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("notificaciones.base-url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("notificaciones.timeout-ms", () -> "500");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void limpiarMocks() {
        wireMockServer.resetAll();
    }

    private LocalDate proximoDiaHabil(int diasAdelante) {
        LocalDate fecha = LocalDate.now().plusDays(diasAdelante);
        while (fecha.getDayOfWeek().getValue() >= 6) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }

    @Test
    @DisplayName("Expone variante MICROSERVICIOS y confirma cita con ENVIADA cuando notificaciones responde 202")
    void reservaExitosaConNotificacionEnviada() {
        ResponseEntity<InfoSistemaDto> infoRes = restTemplate.getForEntity("/api/sistema/info", InfoSistemaDto.class);
        assertThat(infoRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(infoRes.getBody()).isNotNull();
        assertThat(infoRes.getBody().variante()).isEqualTo("MICROSERVICIOS");
        assertThat(infoRes.getBody().arquitecturaInterna()).isEqualTo("HEXAGONAL");
        assertThat(infoRes.getBody().modoConsistencia()).isEqualTo("CP");

        wireMockServer.stubFor(post(urlEqualTo("/api/avisos"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"estado\":\"ENVIADA\"}")));

        LocalDate fechaCita = proximoDiaHabil(7);
        RegistrarCitaRequest req = new RegistrarCitaRequest(
                "40000001", "Ana Pérez", "ana@correo.lab", "MAD",
                fechaCita, LocalTime.of(8, 0), TipoTramite.LEGALIZACION
        );

        ResponseEntity<CitaDto> res = restTemplate.postForEntity("/api/citas", req, CitaDto.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().estado()).isEqualTo("RESERVADA");
        assertThat(res.getBody().notificacion()).isEqualTo("ENVIADA");

        ResponseEntity<ProblemDetail> dupRes = restTemplate.postForEntity("/api/citas", req, ProblemDetail.class);
        assertThat(dupRes.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(dupRes.getBody()).isNotNull();
        assertThat(dupRes.getBody().getProperties()).containsEntry("codigo", "cita-duplicada");
    }

    @Test
    @DisplayName("Cuando notificaciones está caído (WireMock fallo de conexión / 503), reserva responde 201 con PENDIENTE en < 3 s")
    void reservaConNotificacionesCaidoDevuelvePendiente() {
        wireMockServer.stubFor(post(urlEqualTo("/api/avisos"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        LocalDate fechaCita = proximoDiaHabil(12);
        RegistrarCitaRequest req = new RegistrarCitaRequest(
                "40000002", "Luis Gómez", "luis@correo.lab", "MAD",
                fechaCita, LocalTime.of(9, 0), TipoTramite.APOSTILLA
        );

        long inicio = System.currentTimeMillis();
        ResponseEntity<CitaDto> res = restTemplate.postForEntity("/api/citas", req, CitaDto.class);
        long duracionMs = System.currentTimeMillis() - inicio;

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().estado()).isEqualTo("RESERVADA");
        assertThat(res.getBody().notificacion()).isEqualTo("PENDIENTE");
        assertThat(duracionMs).isLessThan(3000L);
    }
}

