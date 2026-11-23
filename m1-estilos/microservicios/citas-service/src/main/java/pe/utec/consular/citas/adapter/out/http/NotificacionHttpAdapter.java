package pe.utec.consular.citas.adapter.out.http;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.utec.consular.citas.application.port.out.NotificacionPort;
import pe.utec.consular.citas.domain.Cita;
import pe.utec.consular.citas.domain.EstadoNotificacion;

import java.time.Duration;

/**
 * Adaptador HTTP de salida que invoca {@code POST /api/avisos} en {@code notificaciones-service}
 * con timeout de 2 segundos y Circuit Breaker Resilience4j ({@code notificaciones}).
 * <p>Concepto: Estilo microservicios, aislamiento de fallas y Circuit Breaker con degradación elegante
 * (secciones 1.2 y 4.3 del contrato global).
 * <p>Sesión: S1. Cuando {@code notificaciones-service} está detenido o falla, el método de fallback
 * confirma la reserva con estado de notificación {@link EstadoNotificacion#PENDIENTE} en menos de 3 segundos.
 */
@Component
public class NotificacionHttpAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacionHttpAdapter.class);

    private final NotificacionesClientProperties properties;
    private final RestClient restClient;

    public NotificacionHttpAdapter(NotificacionesClientProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    @CircuitBreaker(name = "notificaciones", fallbackMethod = "fallbackNotificarReserva")
    public EstadoNotificacion notificarReserva(Cita cita) {
        String url = properties.getBaseUrl() + "/api/avisos";
        log.info("Invocando POST {} para notificar reserva de cita {}", url, cita.getCodigo().valor());

        String asunto = "Confirmación de Cita Consular - " + cita.getCodigo().valor();
        String cuerpo = String.format(
                "Estimado(a) %s, su cita para el trámite de %s ha sido reservada para el día %s a las %s en la oficina %s.",
                cita.getNombres(),
                cita.getTipoTramite(),
                cita.getFecha(),
                cita.getHora(),
                cita.getOficinaId()
        );

        AvisoHttpRequest payload = new AvisoHttpRequest(
                cita.getEmail().valor(),
                asunto,
                cuerpo,
                cita.getCodigo().valor()
        );

        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        log.info("Notificación enviada exitosamente a notificaciones-service para cita {}", cita.getCodigo().valor());
        return EstadoNotificacion.ENVIADA;
    }

    @SuppressWarnings("unused")
    public EstadoNotificacion fallbackNotificarReserva(Cita cita, Throwable causa) {
        log.warn("Fallback del Circuit Breaker 'notificaciones' activado para cita {}: {}. "
                        + "La reserva se confirma con notificación PENDIENTE.",
                cita.getCodigo().valor(), causa.getMessage());
        return EstadoNotificacion.PENDIENTE;
    }
}

