package pe.utec.consular.tramites.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.ConsultarTramiteUseCase;
import pe.utec.consular.tramites.application.port.out.TramiteRepositoryPort;
import pe.utec.consular.tramites.domain.Tramite;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para consultar un trámite consular por su identificador.
 */
@Service
public class ConsultarTramiteService implements ConsultarTramiteUseCase {

    private final TramiteRepositoryPort tramiteRepositoryPort;

    public ConsultarTramiteService(TramiteRepositoryPort tramiteRepositoryPort) {
        this.tramiteRepositoryPort = tramiteRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tramite> consultarPorId(UUID id) {
        return tramiteRepositoryPort.buscarPorId(id);
    }
}

