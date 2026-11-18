package pe.utec.consular.tramites.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.BuscarTramitesPorPaisUseCase;
import pe.utec.consular.tramites.application.port.out.TramiteRepositoryPort;
import pe.utec.consular.tramites.domain.Tramite;

import java.util.List;

/**
 * Caso de uso para buscar trámites cuyo expediente JSONB contenga {@code {"paisDestino": "<pais>"}}.
 */
@Service
public class BuscarTramitesPorPaisService implements BuscarTramitesPorPaisUseCase {

    private final TramiteRepositoryPort tramiteRepositoryPort;

    public BuscarTramitesPorPaisService(TramiteRepositoryPort tramiteRepositoryPort) {
        this.tramiteRepositoryPort = tramiteRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tramite> buscarPorPais(String pais) {
        if (pais == null || pais.isBlank()) {
            throw new IllegalArgumentException("El parámetro 'pais' es obligatorio para la búsqueda documental");
        }
        return tramiteRepositoryPort.buscarPorPaisDestinoEnExpediente(pais);
    }
}

