package pe.utec.consular.tramites.application.port.in;

import java.util.UUID;

/**
 * Comando de entrada para registrar una observación sobre un trámite consular (Asignación A2).
 */
public record ObservarTramiteCommand(
        UUID tramiteId,
        String motivo
) {
}

