package pe.utec.consular.pagos.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Carga útil del evento de integración {@code pago.confirmado} publicado a través de {@code pagos.outbox}
 * hacia RabbitMQ ({@code consular.eventos}, sección 6.1 del contrato global).
 */
public record PagoConfirmado(
        UUID pagoId,
        UUID tramiteId,
        BigDecimal monto
) {
}

