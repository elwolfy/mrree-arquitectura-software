package pe.utec.consular.citas.adapter.out.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para el cliente HTTP hacia {@code notificaciones-service}.
 * <p>Concepto: Configuración externalizada para comunicación entre microservicios (regla 02).
 */
@ConfigurationProperties(prefix = "notificaciones")
public class NotificacionesClientProperties {

    private String baseUrl = "http://notificaciones:8084";
    private int timeoutMs = 2000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}

