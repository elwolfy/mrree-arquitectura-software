package pe.utec.consular.identidad;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
import pe.utec.consular.identidad.adapter.in.web.CiudadanoDto;
import pe.utec.consular.identidad.adapter.in.web.ValidarIdentidadRequest;
import pe.utec.consular.identidad.adapter.out.pide.ReniecPideAdapter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integracion")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas de integración de identidad-service contra el simulador WireMock de PIDE-RENIEC")
class IdentidadIntegracionTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void iniciarWireMock() {
        Path rutaSimulador = Path.of("../pide-simulador");
        if (!Files.exists(rutaSimulador)) {
            rutaSimulador = Path.of("pide-simulador");
        }
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .dynamicPort()
                        .usingFilesUnderDirectory(rutaSimulador.toAbsolutePath().normalize().toString())
                        .globalTemplating(true)
        );
        wireMockServer.start();
    }

    @AfterAll
    static void detenerWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("pide.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ReniecPideAdapter reniecPideAdapter;

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
    void restaurarEscenarioYCircuitBreaker() {
        wireMockServer.resetScenarios();
        reniecPideAdapter.resetearCircuitBreaker();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("DNI 40000001 devuelve 200 OK con el modelo propio Ciudadano (vigente=true)")
    void dniValidoDevuelve200ConCiudadano() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CiudadanoDto> response = restTemplate.postForEntity(
                url("/api/identidad/validaciones"),
                new HttpEntity<>(new ValidarIdentidadRequest("40000001"), headers),
                CiudadanoDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CiudadanoDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals("40000001", dto.dni());
        assertEquals("CIUDADANO 40000001", dto.nombres());
        assertEquals("PEREZ MENDOZA", dto.apellidos());
        assertTrue(dto.vigente());
    }

    @Test
    @DisplayName("DNI 49999999 (fallecido en RENIEC) devuelve 422 ProblemDetail con título 'Identidad no vigente'")
    void dniFallecidoDevuelve422IdentidadNoVigente() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/identidad/validaciones"),
                new HttpEntity<>(new ValidarIdentidadRequest("49999999"), headers),
                Map.class
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Identidad no vigente", response.getBody().get("title"));
        assertEquals("identidad-no-vigente", response.getBody().get("codigo"));
    }

    @Test
    @DisplayName("DNI 48888888 (no existe en RENIEC) devuelve 404 ProblemDetail con título 'DNI no encontrado'")
    void dniNoEncontradoDevuelve404() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/identidad/validaciones"),
                new HttpEntity<>(new ValidarIdentidadRequest("48888888"), headers),
                Map.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DNI no encontrado", response.getBody().get("title"));
        assertEquals("dni-no-encontrado", response.getBody().get("codigo"));
    }

    @Test
    @DisplayName("Al activarse el escenario CAIDO en WireMock (503), ejecuta reintentos, abre el Circuit Breaker y responde 503 'Servicio de identidad no disponible'")
    void escenarioCaidoDevuelve503ServicioNoDisponible() {
        // Activar escenario de caída por la API de administración de WireMock
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.put(
                "http://localhost:" + wireMockServer.port() + "/__admin/scenarios/EstadoPide/state",
                new HttpEntity<>(Map.of("state", "CAIDO"), headers)
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/identidad/validaciones"),
                new HttpEntity<>(new ValidarIdentidadRequest("40000001"), headers),
                Map.class
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Servicio de identidad no disponible", response.getBody().get("title"));
        assertEquals("servicio-identidad-no-disponible", response.getBody().get("codigo"));
    }
}

