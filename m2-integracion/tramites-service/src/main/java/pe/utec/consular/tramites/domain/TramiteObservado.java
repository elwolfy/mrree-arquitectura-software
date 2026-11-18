package pe.utec.consular.tramites.domain;

import java.util.UUID;

/**
 * Carga útil del evento de integración {@code tramite.observado} publicado en RabbitMQ
 * cuando un trámite pasa al estado {@link EstadoTramite#OBSERVADO} (Asignación A2).
 */
public record TramiteObservado(
        UUID tramiteId,
        String codigo,
        String dni,
        String email,
        String motivo
) {
}

