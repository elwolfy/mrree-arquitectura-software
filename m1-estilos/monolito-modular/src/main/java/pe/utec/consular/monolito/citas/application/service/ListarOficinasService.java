package pe.utec.consular.monolito.citas.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.monolito.citas.application.port.in.ListarOficinasUseCase;
import pe.utec.consular.monolito.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.OficinaConsular;

import java.util.List;

/**
 * Caso de uso para obtener el catálogo de oficinas consulares activas.
 * <p>Concepto: Arquitectura hexagonal – caso de uso (sección 1.3).
 */
@Service
@Transactional(readOnly = true)
public class ListarOficinasService implements ListarOficinasUseCase {

    private final OficinaRepositoryPort oficinaRepositoryPort;

    public ListarOficinasService(OficinaRepositoryPort oficinaRepositoryPort) {
        this.oficinaRepositoryPort = oficinaRepositoryPort;
    }

    @Override
    public List<OficinaConsular> listar() {
        return oficinaRepositoryPort.listarTodas();
    }
}
