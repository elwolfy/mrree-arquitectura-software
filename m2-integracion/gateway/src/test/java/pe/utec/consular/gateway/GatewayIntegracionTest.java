package pe.utec.consular.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integracion")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas de integración del API Gateway (X-Correlation-Id y Rate Limiting 429)")
class GatewayIntegracionTest {

    private static WireMockServer backendMock;

    @BeforeAll
    static void iniciarWireMock() {
        backendMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        backendMock.start();

        backendMock.stubFor(WireMock.post(urlEqualTo("/api/identidad/validaciones"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"dni\":\"40000001\",\"nombres\":\"ANA\",\"apellidos\":\"PEREZ\",\"vigente\":true}")));
    }

    @AfterAll
    static void detenerWireMock() {
        if (backendMock != null && backendMock.isRunning()) {
            backendMock.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("IDENTIDAD_SERVICE_URL", () -> "http://localhost:" + backendMock.port());
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

    @Test
    @DisplayName("Debe generar y propagar X-Correlation-Id y responder 429 al superar las 5 solicitudes por segundo en /api/identidad/**")
    void verificaCorrelationIdYRateLimiting429() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("dni", "40000001"), headers);

        String url = "http://localhost:" + port + "/api/identidad/validaciones";

        // 1. Primera solicitud: verifica generación y propagación de X-Correlation-Id
        ResponseEntity<Map> primera = restTemplate.postForEntity(url, entity, Map.class);
        assertEquals(HttpStatus.OK, primera.getStatusCode());
        String correlationId = primera.getHeaders().getFirst("X-Correlation-Id");
        assertNotNull(correlationId, "El Gateway debe devolver la cabecera X-Correlation-Id");

        backendMock.verify(postRequestedFor(urlEqualTo("/api/identidad/validaciones"))
                .withHeader("X-Correlation-Id", matching(".+")));

        // 2. Ráfaga de 7 solicitudes adicionales en el mismo segundo -> al menos una debe recibir 429 Too Many Requests
        List<HttpStatusCode> codigos = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, entity, Map.class);
            codigos.add(resp.getStatusCode());
        }

        assertTrue(codigos.contains(HttpStatus.TOO_MANY_REQUESTS),
                "Al superar el límite de 5 req/s hacia /api/identidad/**, el Gateway debe responder 429 Too Many Requests");
    }
}

