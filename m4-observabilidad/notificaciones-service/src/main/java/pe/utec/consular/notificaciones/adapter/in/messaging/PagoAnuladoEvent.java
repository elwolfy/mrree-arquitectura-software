package pe.utec.consular.notificaciones.adapter.in.messaging;

import java.util.UUID;

/**
 * Carga útil del evento {@code pago.anulado} recibido por {@code notificaciones-service}.
 */
public record PagoAnuladoEvent(
        UUID pagoId,
        UUID tramiteId,
        String motivo
) {
}

