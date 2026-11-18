package pe.utec.consular.tramites.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.ListarTramitesPorDniUseCase;
import pe.utec.consular.tramites.application.port.out.TramiteRepositoryPort;
import pe.utec.consular.tramites.domain.Dni;
import pe.utec.consular.tramites.domain.Tramite;

import java.util.List;

/**
 * Caso de uso para listar los trámites consulares asociados al DNI de un ciudadano.
 */
@Service
public class ListarTramitesPorDniService implements ListarTramitesPorDniUseCase {

    private final TramiteRepositoryPort tramiteRepositoryPort;

    public ListarTramitesPorDniService(TramiteRepositoryPort tramiteRepositoryPort) {
        this.tramiteRepositoryPort = tramiteRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tramite> listarPorDni(String dni) {
        Dni dniValidado = new Dni(dni);
        return tramiteRepositoryPort.buscarPorDni(dniValidado.valor());
    }
}

