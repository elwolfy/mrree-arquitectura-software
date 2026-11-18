package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.Tramite;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para consultar un trámite consular por su identificador único.
 * Reutilizado tanto por el adaptador REST (:8082) como por el adaptador gRPC (:9082).
 */
public interface ConsultarTramiteUseCase {

    Optional<Tramite> consultarPorId(UUID id);
}

