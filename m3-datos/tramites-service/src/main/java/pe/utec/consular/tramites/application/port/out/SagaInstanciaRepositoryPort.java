package pe.utec.consular.tramites.application.port.out;

import pe.utec.consular.tramites.domain.SagaInstancia;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario para persistir el estado de cada instancia de saga en {@code tramites.saga_instancia}.
 */
public interface SagaInstanciaRepositoryPort {

    SagaInstancia guardar(SagaInstancia instancia);

    Optional<SagaInstancia> buscarPorTramiteId(UUID tramiteId);
}

