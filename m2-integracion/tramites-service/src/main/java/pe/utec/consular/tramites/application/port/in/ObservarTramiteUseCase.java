package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.Tramite;

/**
 * Puerto de entrada para observar un trámite consular que requiere corrección del ciudadano (Asignación A2).
 */
public interface ObservarTramiteUseCase {

    Tramite observar(ObservarTramiteCommand comando);
}

