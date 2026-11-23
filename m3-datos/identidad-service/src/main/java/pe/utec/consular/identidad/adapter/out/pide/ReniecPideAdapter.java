package pe.utec.consular.identidad.adapter.out.pide;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.utec.consular.identidad.application.port.out.ValidarIdentidadPort;
import pe.utec.consular.identidad.domain.Ciudadano;
import pe.utec.consular.identidad.domain.Dni;
import pe.utec.consular.identidad.domain.exception.ServicioIdentidadNoDisponibleException;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Adaptador de salida y Capa Anticorrupción (ACL) hacia el servicio simulado de RENIEC en la PIDE.
 * <p>Conceptos arquitectónicos que materializa esta clase:
 * <ul>
 *   <li><b>Patrón Estructural Adapter (GoF – sección 2.1):</b> Convierte la interfaz esperada por
 *       nuestro dominio ({@link ValidarIdentidadPort#consultarPorDni(Dni)}) en una invocación HTTP
 *       hacia un contrato incompatible ({@code POST /ws/reniec/consultarDatos}).</li>
 *   <li><b>Capa Anticorrupción / Anti-Corruption Layer (DDD Context Map – sección 2.4):</b> Aísla
 *       nuestro Bounded Context de Identidad frente a un sistema externo que no controlamos (PIDE/RENIEC).
 *       Recibe el DTO externo {@link ReniecRespuesta} con su nomenclatura legada ({@code coResultado},
 *       {@code prenombres}, {@code apPrimer}, {@code inFallecido}) y lo traduce mediante
 *       {@link ReniecPideTranslator} al modelo limpio {@link Ciudadano}, impidiendo que los nombres
 *       externos se filtren hacia {@code domain} o {@code application}.</li>
 *   <li><b>Resiliencia (Resilience4j):</b> Aplica un tiempo máximo de espera de 2 s, 2 reintentos
 *       ante fallas de red o HTTP 5xx y un Circuit Breaker que, al abrirse, responde inmediatamente
 *       con {@link ServicioIdentidadNoDisponibleException} (HTTP 503).</li>
 * </ul>
 * <p>Sesión: S3.
 */
@Component
public class ReniecPideAdapter implements ValidarIdentidadPort {

    private static final Logger log = LoggerFactory.getLogger(ReniecPideAdapter.class);
    private static final String RUTA_CONSULTA_RENIEC = "/ws/reniec/consultarDatos";

    private final RestClient restClient;
    private final ReniecPideTranslator translator;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ReniecPideAdapter(
            @Value("${pide.url:http://pide-simulador:8080}") String pideUrl,
            @Value("${pide.timeout-ms:2000}") int timeoutMs,
            @Value("${pide.reintentos:2}") int maxReintentos,
            ReniecPideTranslator translator) {
        this.translator = translator;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(pideUrl)
                .requestFactory(requestFactory)
                .build();

        // 1 intento inicial + 2 reintentos = 3 intentos máximos ante RestClientException (incluye 5xx y timeouts)
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(1 + Math.max(0, maxReintentos))
                .waitDuration(Duration.ofMillis(100))
                .retryExceptions(RestClientException.class)
                .build();
        this.retry = Retry.of("pideReniecRetry", retryConfig);

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .recordExceptions(RestClientException.class)
                .build();
        this.circuitBreaker = CircuitBreaker.of("pideReniecCircuitBreaker", cbConfig);
    }

    @Override
    public Ciudadano consultarPorDni(Dni dni) {
        Supplier<ReniecRespuesta> llamadaRemota = () -> {
            log.info("Invocando PIDE-RENIEC {} para DNI {}", RUTA_CONSULTA_RENIEC, dni.enmascarado());
            return restClient.post()
                    .uri(RUTA_CONSULTA_RENIEC)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReniecConsultaRequest(dni.valor()))
                    .retrieve()
                    .body(ReniecRespuesta.class);
        };

        Supplier<ReniecRespuesta> decorada = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(retry, llamadaRemota)
        );

        final ReniecRespuesta respuesta;
        try {
            respuesta = decorada.get();
        } catch (CallNotPermittedException ex) {
            log.error("Circuit Breaker abierto hacia PIDE-RENIEC al consultar DNI {}", dni.enmascarado());
            throw new ServicioIdentidadNoDisponibleException(
                    "Servicio de identidad no disponible (circuito abierto hacia PIDE-RENIEC)", ex);
        } catch (RestClientException ex) {
            log.error("Falla de comunicación con PIDE-RENIEC tras reintentos para DNI {}: {}",
                    dni.enmascarado(), ex.getMessage());
            throw new ServicioIdentidadNoDisponibleException(
                    "Servicio de identidad no disponible: falla en proveedor PIDE-RENIEC", ex);
        }

        return translator.aDominio(dni, respuesta);
    }

    /**
     * Reinicia el estado del Circuit Breaker (útil entre casos de prueba de integración).
     */
    public void resetearCircuitBreaker() {
        this.circuitBreaker.reset();
    }
}

