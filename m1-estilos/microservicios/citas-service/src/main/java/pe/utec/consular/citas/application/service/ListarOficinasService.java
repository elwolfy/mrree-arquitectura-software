package pe.utec.consular.citas.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.citas.application.port.in.ListarOficinasUseCase;
import pe.utec.consular.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;

/**
 * Servicio de aplicación para listar el catálogo de oficinas consulares.
 */
@Service
public class ListarOficinasService implements ListarOficinasUseCase {

    private final OficinaRepositoryPort oficinaRepositoryPort;

    public ListarOficinasService(OficinaRepositoryPort oficinaRepositoryPort) {
        this.oficinaRepositoryPort = oficinaRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OficinaConsular> listarTodas() {
        return oficinaRepositoryPort.listarTodas();
    }
}

