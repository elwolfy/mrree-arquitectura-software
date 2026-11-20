package pe.utec.consular.emision.adapter.in.messaging;

import java.util.UUID;

/**
 * Carga útil del comando {@code emision.solicitar} consumido desde {@code emision.comandos}.
 */
public record EmisionSolicitarCommand(
        UUID tramiteId,
        String codigo,
        String tipoTramite
) {
}

