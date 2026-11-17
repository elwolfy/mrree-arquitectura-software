package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.TipoTramite;

/**
 * Comando de entrada para registrar un nuevo trámite consular.
 */
public record RegistrarTramiteCommand(
        String dni,
        String nombres,
        String email,
        TipoTramite tipoTramite,
        String pais,
        int folios,
        String idioma
) {
}

