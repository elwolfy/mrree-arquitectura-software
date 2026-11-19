package pe.utec.consular.tramites.cache;

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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.tramites.adapter.in.web.RegistrarTramiteRequest;
import pe.utec.consular.tramites.adapter.in.web.TramiteDto;
import pe.utec.consular.tramites.application.saga.SagaSolicitudTramite;
import pe.utec.consular.tramites.domain.PagoConfirmado;
import pe.utec.consular.tramites.domain.TipoTramite;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Asignación A4")
@Tag("integracion")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "grpc.port=0",
                "cache.consulta-tramite.tipo=redis",
                "cache.consulta-tramite.ttl-segundos=60"
        }
)
@DisplayName("Asignación A4 — Caché Redis Cache-Aside (TTL 60 s) e invalidación al cambiar estado en GET /api/tramites/{id}")
class RedisCacheConsultaTramiteAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("consular")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("01-esquemas.sql");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "tramites_app");
        registry.add("spring.datasource.password", () -> "tramites_pwd");
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @LocalServerPort
    private int httpPort;

    @Autowired
    private SagaSolicitudTramite sagaSolicitudTramite;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    @DisplayName("GET /api/tramites/{id} devuelve X-Cache: MISS en la primera lectura, X-Cache: HIT en la segunda, y X-Cache: MISS tras un cambio de estado")
    void cacheAsideDevuelveHitEnSegundaLecturaYMssTrasInvalidarAlCambiarEstado() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RegistrarTramiteRequest req = new RegistrarTramiteRequest(
                "40000001", "Ana Pérez", "ana@correo.lab",
                TipoTramite.APOSTILLA, "ES", 2, "ES"
        );
        ResponseEntity<TramiteDto> creado = restTemplate.postForEntity(
                "http://localhost:" + httpPort + "/api/tramites",
                new HttpEntity<>(req, headers),
                TramiteDto.class
        );
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());
        assertNotNull(creado.getBody());
        UUID tramiteId = creado.getBody().id();
        String urlConsulta = "http://localhost:" + httpPort + "/api/tramites/" + tramiteId;

        // 1. Primera lectura: no está en Redis -> X-Cache: MISS y se puebla el caché
        ResponseEntity<TramiteDto> lectura1 = restTemplate.getForEntity(urlConsulta, TramiteDto.class);
        assertEquals(HttpStatus.OK, lectura1.getStatusCode());
        assertEquals("MISS", lectura1.getHeaders().getFirst("X-Cache"));

        // 2. Segunda lectura: servida desde Redis -> X-Cache: HIT
        ResponseEntity<TramiteDto> lectura2 = restTemplate.getForEntity(urlConsulta, TramiteDto.class);
        assertEquals(HttpStatus.OK, lectura2.getStatusCode());
        assertEquals("HIT", lectura2.getHeaders().getFirst("X-Cache"));

        // 3. Cambio de estado en la proyección CQRS -> invalida la clave en Redis
        sagaSolicitudTramite.alConfirmarPago(new PagoConfirmado(UUID.randomUUID(), tramiteId, creado.getBody().monto()));

        // 4. Tercera lectura tras invalidación -> X-Cache: MISS con el nuevo estado
        ResponseEntity<TramiteDto> lectura3 = restTemplate.getForEntity(urlConsulta, TramiteDto.class);
        assertEquals(HttpStatus.OK, lectura3.getStatusCode());
        assertEquals("MISS", lectura3.getHeaders().getFirst("X-Cache"));
    }
}

