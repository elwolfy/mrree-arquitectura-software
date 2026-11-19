package pe.utec.consular.notificaciones.adapter.in.messaging;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Carga útil del evento {@code pago.confirmado} recibido por {@code notificaciones-service}.
 */
public record PagoConfirmadoEvent(
        UUID pagoId,
        UUID tramiteId,
        BigDecimal monto
) {
}

