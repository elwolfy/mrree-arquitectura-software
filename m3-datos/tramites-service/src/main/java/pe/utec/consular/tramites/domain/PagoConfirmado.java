package pe.utec.consular.tramites.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento de integración {@code pago.confirmado} recibido por el orquestador de la saga.
 */
public record PagoConfirmado(
        UUID pagoId,
        UUID tramiteId,
        BigDecimal monto
) {
}

