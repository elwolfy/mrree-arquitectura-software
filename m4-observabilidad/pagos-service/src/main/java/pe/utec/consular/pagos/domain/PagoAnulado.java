package pe.utec.consular.pagos.domain;

import java.util.UUID;

/**
 * Carga útil del evento de integración {@code pago.anulado} publicado a través de {@code pagos.outbox}
 * hacia RabbitMQ ({@code consular.eventos}, secciones 6.1 y 8 del contrato global).
 */
public record PagoAnulado(
        UUID pagoId,
        UUID tramiteId,
        String motivo
) {
}

