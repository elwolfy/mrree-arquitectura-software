package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.Tramite;

/**
 * Puerto de entrada para el caso de uso de registro de un trámite consular.
 */
public interface RegistrarTramiteUseCase {

    Tramite registrar(RegistrarTramiteCommand comando);
}

