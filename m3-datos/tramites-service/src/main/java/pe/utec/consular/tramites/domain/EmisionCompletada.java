package pe.utec.consular.tramites.domain;

import java.util.UUID;

/**
 * Evento de integración {@code emision.completada} recibido por el orquestador de la saga.
 */
public record EmisionCompletada(
        UUID tramiteId,
        String numeroDocumento
) {
}

