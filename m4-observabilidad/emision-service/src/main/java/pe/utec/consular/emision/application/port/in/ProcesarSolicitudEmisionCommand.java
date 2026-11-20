package pe.utec.consular.emision.application.port.in;

import java.util.UUID;

/**
 * Comando recibido desde la cola {@code emision.comandos} ({@code emision.solicitar}).
 */
public record ProcesarSolicitudEmisionCommand(
        String messageId,
        UUID tramiteId
) {
}

