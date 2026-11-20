package pe.utec.consular.tramites.domain;

import java.util.UUID;

/**
 * Evento de integración {@code emision.fallida} recibido por el orquestador de la saga.
 */
public record EmisionFallida(
        UUID tramiteId,
        String motivo
) {
}

