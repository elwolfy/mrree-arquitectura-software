package pe.utec.consular.citas.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.citas.application.port.in.ListarOficinasUseCase;
import pe.utec.consular.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;

/**
 * Caso de uso para listar todas las oficinas consulares registradas.
 * <p>Concepto: Arquitectura hexagonal – caso de uso (sección 1.3).
 * <p>Sesión: S3.
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

