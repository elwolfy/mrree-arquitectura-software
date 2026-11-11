package pe.utec.consular.monolito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.monolito.citas.adapter.in.web.CitaDto;
import pe.utec.consular.monolito.citas.adapter.in.web.DisponibilidadDto;
import pe.utec.consular.monolito.citas.adapter.in.web.InfoSistemaDto;
import pe.utec.consular.monolito.citas.adapter.in.web.OficinaDto;
import pe.utec.consular.monolito.citas.adapter.in.web.RegistrarCitaRequest;
import pe.utec.consular.monolito.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cita;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;
import pe.utec.consular.monolito.compartido.infraestructura.NotificacionesProperties;
import pe.utec.consular.monolito.tramites.adapter.in.web.TarifaDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integracion")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas de integración end-to-end con Testcontainers PostgreSQL")
class CitaIntegracionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("consular")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        registry.add("consistencia.modo", () -> "CP");
        registry.add("notificaciones.canal", () -> "CONSOLA");
        registry.add("notificaciones.simular-falla", () -> "false");
    }

    @LocalServerPort
    private int port;

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

    @Autowired
    private CitaRepositoryPort citaRepositoryPort;

    @Autowired
    private NotificacionesProperties notificacionesProperties;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("Flujo completo: Reserva exitosa (201), duplicado (409), sin capacidad (409) y rollback por falla (500)")
    void flujoCompletoDeCitas() {
        LocalDate fechaCita = LocalDate.now().plusDays(7);
        while (fechaCita.getDayOfWeek().getValue() >= 6) {
            fechaCita = fechaCita.plusDays(1);
        }
        LocalTime horaCita = LocalTime.of(8, 0);

        // 1. Catálogo de oficinas consulares
        ResponseEntity<OficinaDto[]> respOficinas = restTemplate.getForEntity(url("/api/oficinas"), OficinaDto[].class);
        assertEquals(HttpStatus.OK, respOficinas.getStatusCode());
        assertNotNull(respOficinas.getBody());
        assertTrue(respOficinas.getBody().length >= 6);

        // 2. Consulta de disponibilidad
        ResponseEntity<DisponibilidadDto> respDisp = restTemplate.getForEntity(
                url("/api/oficinas/MAD/disponibilidad?fecha=" + fechaCita), DisponibilidadDto.class);
        assertEquals(HttpStatus.OK, respDisp.getStatusCode());
        assertNotNull(respDisp.getBody());
        assertEquals("BASE_DE_DATOS", respDisp.getBody().origen());

        // 3. Primera reserva exitosa (201 Created)
        RegistrarCitaRequest peticion1 = new RegistrarCitaRequest(
                "40000001",
                "Ana Pérez",
                "ana@correo.lab",
                "MAD",
                fechaCita,
                horaCita,
                TipoTramite.LEGALIZACION
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegistrarCitaRequest> entidad1 = new HttpEntity<>(peticion1, headers);

        ResponseEntity<CitaDto> respReserva1 = restTemplate.postForEntity(url("/api/citas"), entidad1, CitaDto.class);
        assertEquals(HttpStatus.CREATED, respReserva1.getStatusCode());
        assertNotNull(respReserva1.getHeaders().getLocation());
        CitaDto cita1 = respReserva1.getBody();
        assertNotNull(cita1);
        assertEquals("RESERVADA", cita1.estado().name());
        assertEquals("40000001", cita1.dni());
        assertEquals("MAD", cita1.oficinaId());

        // 4. Intento de cita duplicada para el mismo ciudadano en la misma fecha y sede -> 409
        ResponseEntity<Map> respDuplicada = restTemplate.postForEntity(url("/api/citas"), entidad1, Map.class);
        assertEquals(HttpStatus.CONFLICT, respDuplicada.getStatusCode());
        assertNotNull(respDuplicada.getBody());
        assertEquals("cita-duplicada", respDuplicada.getBody().get("codigo"));

        // 5. Segunda reserva para agotar el cupo (capacidad = 2)
        RegistrarCitaRequest peticion2 = new RegistrarCitaRequest(
                "40000002",
                "Carlos Gómez",
                "carlos@correo.lab",
                "MAD",
                fechaCita,
                horaCita,
                TipoTramite.LEGALIZACION
        );
        ResponseEntity<CitaDto> respReserva2 = restTemplate.postForEntity(
                url("/api/citas"), new HttpEntity<>(peticion2, headers), CitaDto.class);
        assertEquals(HttpStatus.CREATED, respReserva2.getStatusCode());

        // 6. Tercera reserva sobre el mismo cupo -> 409 cupo-sin-capacidad
        RegistrarCitaRequest peticion3 = new RegistrarCitaRequest(
                "40000003",
                "Beatriz Ramos",
                "beatriz@correo.lab",
                "MAD",
                fechaCita,
                horaCita,
                TipoTramite.LEGALIZACION
        );
        ResponseEntity<Map> respSinCapacidad = restTemplate.postForEntity(
                url("/api/citas"), new HttpEntity<>(peticion3, headers), Map.class);
        assertEquals(HttpStatus.CONFLICT, respSinCapacidad.getStatusCode());
        assertNotNull(respSinCapacidad.getBody());
        assertEquals("cupo-sin-capacidad", respSinCapacidad.getBody().get("codigo"));

        // 7. Simulación de falla en notificación -> 500 y Rollback completo en base de datos
        notificacionesProperties.setSimularFalla(true);
        RegistrarCitaRequest peticionConFalla = new RegistrarCitaRequest(
                "40000004",
                "Diana Salazar",
                "diana@correo.lab",
                "MAD",
                fechaCita,
                LocalTime.of(8, 30),
                TipoTramite.APOSTILLA
        );
        try {
            ResponseEntity<Map> respFalla = restTemplate.postForEntity(
                    url("/api/citas"), new HttpEntity<>(peticionConFalla, headers), Map.class);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respFalla.getStatusCode());

            // Verificar que la cita NO quedó guardada en BD gracias al rollback de @Transactional
            List<Cita> citasDni4 = citaRepositoryPort.buscarPorDni("40000004");
            assertTrue(citasDni4.isEmpty(), "La transacción debió revertirse y no dejar rastro en la base de datos");
        } finally {
            notificacionesProperties.setSimularFalla(false);
        }

        // 8. Consulta de citas por DNI
        ResponseEntity<CitaDto[]> respDni = restTemplate.getForEntity(
                url("/api/citas?dni=40000001"), CitaDto[].class);
        assertEquals(HttpStatus.OK, respDni.getStatusCode());
        assertNotNull(respDni.getBody());
        assertEquals(1, respDni.getBody().length);
        assertEquals(cita1.id(), respDni.getBody()[0].id());

        // 9. Consulta de tarifas
        ResponseEntity<TarifaDto> respTarifa = restTemplate.getForEntity(
                url("/api/tarifas?tipoTramite=APOSTILLA&pais=ES"), TarifaDto.class);
        assertEquals(HttpStatus.OK, respTarifa.getStatusCode());
        assertNotNull(respTarifa.getBody());
        assertEquals(new BigDecimal("33.00"), respTarifa.getBody().monto());
        assertEquals("ApostillaTarifaStrategy", respTarifa.getBody().estrategia());

        ResponseEntity<Map> respTarifaInvalida = restTemplate.getForEntity(
                url("/api/tarifas?tipoTramite=REGISTRO_CONSULAR&pais=ES"), Map.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, respTarifaInvalida.getStatusCode());

        // 10. Información de sistema
        ResponseEntity<InfoSistemaDto> respInfo = restTemplate.getForEntity(
                url("/api/sistema/info"), InfoSistemaDto.class);
        assertEquals(HttpStatus.OK, respInfo.getStatusCode());
        assertNotNull(respInfo.getBody());
        assertEquals("MONOLITO_MODULAR", respInfo.getBody().variante());
        assertEquals("HEXAGONAL", respInfo.getBody().arquitecturaInterna());
        assertEquals("CP", respInfo.getBody().modoConsistencia());

        // 11. Cancelación de cita (204 No Content)
        ResponseEntity<Void> respCancel = restTemplate.exchange(
                url("/api/citas/" + cita1.id()), HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, respCancel.getStatusCode());
    }
}
