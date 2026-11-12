package pe.utec.consular.notificaciones;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.notificaciones.adapter.in.web.AvisoDto;
import pe.utec.consular.notificaciones.adapter.in.web.AvisoRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración de {@code notificaciones-service} con Testcontainers PostgreSQL 16.
 * <p>Concepto: Pruebas de integración en microservicios (regla 04).
 * <p>Sesión: S1.
 */
@Tag("integracion")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas de integración de notificaciones-service (Testcontainers)")
class NotificacionesIntegracionTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("consular")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> postgres.getJdbcUrl() + "&currentSchema=notificaciones");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Registra un aviso (202 Accepted) y lo consulta por referencia (200 OK)")
    void registraYConsultaAvisoPorReferencia() {
        AvisoRequest request = new AvisoRequest(
                "ana@correo.lab",
                "Confirmación de Cita Consular - CIT-2026-000001",
                "Estimado(a) Ana Pérez, su cita ha sido reservada.",
                "CIT-2026-000001"
        );

        ResponseEntity<AvisoDto> postRes = restTemplate.postForEntity("/api/avisos", request, AvisoDto.class);
        assertThat(postRes.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(postRes.getBody()).isNotNull();
        assertThat(postRes.getBody().id()).isNotNull();
        assertThat(postRes.getBody().estado()).isEqualTo("ENVIADA");
        assertThat(postRes.getBody().canal()).isEqualTo("CONSOLA");
        assertThat(postRes.getBody().referencia()).isEqualTo("CIT-2026-000001");

        ResponseEntity<AvisoDto[]> getRes = restTemplate.getForEntity(
                "/api/avisos?referencia=CIT-2026-000001", AvisoDto[].class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getRes.getBody()).isNotNull().hasSize(1);
        assertThat(getRes.getBody()[0].destinatario()).isEqualTo("ana@correo.lab");
    }
}

