package pe.utec.consular.bff.client;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

/**
 * Configuración de los clientes HTTP declarativos ({@code @HttpExchange}) de {@code bff-web}.
 * <p>Todos los clientes apuntan directamente a los nombres de contenedor de cada microservicio
 * (nunca a través del API Gateway) y propagan la cabecera {@code X-Correlation-Id} desde el MDC.
 */
@Configuration
public class ClientesHttpConfig {

    private static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Bean
    public CitasClient citasClient(
            @Value("${servicios.citas-url:http://citas-service:8081}") String baseUrl,
            @Value("${servicios.timeout-ms:2000}") int timeoutMs) {
        return crearProxy(baseUrl, timeoutMs, CitasClient.class);
    }

    @Bean
    public TramitesClient tramitesClient(
            @Value("${servicios.tramites-url:http://tramites-service:8082}") String baseUrl,
            @Value("${servicios.timeout-ms:2000}") int timeoutMs) {
        return crearProxy(baseUrl, timeoutMs, TramitesClient.class);
    }

    @Bean
    public NotificacionesClient notificacionesClient(
            @Value("${servicios.notificaciones-url:http://notificaciones-service:8084}") String baseUrl,
            @Value("${servicios.timeout-ms:2000}") int timeoutMs) {
        return crearProxy(baseUrl, timeoutMs, NotificacionesClient.class);
    }

    @Bean
    public IdentidadClient identidadClient(
            @Value("${servicios.identidad-url:http://identidad-service:8086}") String baseUrl,
            @Value("${servicios.timeout-ms:2000}") int timeoutMs) {
        return crearProxy(baseUrl, timeoutMs, IdentidadClient.class);
    }

    @Bean
    public PagosClient pagosClient(
            @Value("${servicios.pagos-url:http://pagos-service:8083}") String baseUrl,
            @Value("${servicios.timeout-ms:2000}") int timeoutMs) {
        return crearProxy(baseUrl, timeoutMs, PagosClient.class);
    }

    private <T> T crearProxy(String baseUrl, int timeoutMs, Class<T> clientType) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

        ClientHttpRequestInterceptor correlationInterceptor = (request, body, execution) -> {
            String correlationId = MDC.get(MDC_KEY);
            if (correlationId != null && !correlationId.isBlank()) {
                request.getHeaders().set(HEADER_CORRELATION_ID, correlationId);
            }
            return execution.execute(request, body);
        };

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(correlationInterceptor)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(clientType);
    }
}

