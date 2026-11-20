package pe.utec.consular.notificaciones.adapter.in.messaging;

import java.util.UUID;

/**
 * Carga útil del evento {@code emision.completada} recibido por {@code notificaciones-service}.
 */
public record EmisionCompletadaEvent(
        UUID tramiteId,
        String numeroDocumento
) {
}

