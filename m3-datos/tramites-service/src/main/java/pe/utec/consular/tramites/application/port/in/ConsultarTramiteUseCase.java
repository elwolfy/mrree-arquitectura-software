package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.ResultadoConsultaTramite;
import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para consultar un trámite consular por su identificador único,
 * aplicando la estrategia <b>Cache-Aside</b> delante de {@code tramites.vista_tramite}.
 */
public interface ConsultarTramiteUseCase {

    Optional<VistaTramite> consultarPorId(UUID id);

    Optional<ResultadoConsultaTramite> consultarConOrigenCache(UUID id);
}
