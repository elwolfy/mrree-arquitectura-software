package pe.utec.consular.emision.domain;

import java.util.UUID;

/**
 * Carga útil del evento de integración {@code emision.fallida} publicado hacia RabbitMQ
 * cuando la generación del documento consular falla (sección 6.1 del contrato global).
 */
public record EmisionFallida(
        UUID tramiteId,
        String motivo
) {
}

