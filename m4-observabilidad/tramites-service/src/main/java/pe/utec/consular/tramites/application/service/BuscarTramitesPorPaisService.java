package pe.utec.consular.tramites.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.BuscarTramitesPorPaisUseCase;
import pe.utec.consular.tramites.application.port.out.VistaTramiteRepositoryPort;
import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.List;

/**
 * Caso de uso para buscar trámites cuyo expediente JSONB contenga {@code {"paisDestino": "<pais>"}},
 * proyectados desde {@code tramites.vista_tramite}.
 */
@Service
public class BuscarTramitesPorPaisService implements BuscarTramitesPorPaisUseCase {

    private final VistaTramiteRepositoryPort vistaTramiteRepositoryPort;

    public BuscarTramitesPorPaisService(VistaTramiteRepositoryPort vistaTramiteRepositoryPort) {
        this.vistaTramiteRepositoryPort = vistaTramiteRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VistaTramite> buscarPorPais(String pais) {
        if (pais == null || pais.isBlank()) {
            throw new IllegalArgumentException("El parámetro 'pais' es obligatorio para la búsqueda documental");
        }
        return vistaTramiteRepositoryPort.buscarPorPaisDestinoEnExpediente(pais);
    }
}
