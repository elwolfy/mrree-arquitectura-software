package pe.utec.consular.emision.domain;

import java.util.UUID;

/**
 * Carga útil del evento de integración {@code emision.completada} publicado hacia RabbitMQ
 * ({@code consular.eventos}, sección 6.1 del contrato global).
 */
public record EmisionCompletada(
        UUID tramiteId,
        String numeroDocumento
) {
}

