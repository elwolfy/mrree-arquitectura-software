package pe.utec.consular.tramites.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.ListarTramitesPorDniUseCase;
import pe.utec.consular.tramites.application.port.out.VistaTramiteRepositoryPort;
import pe.utec.consular.tramites.domain.Dni;
import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.List;

/**
 * Caso de uso para listar todos los trámites de un ciudadano por su DNI,
 * leyendo <b>exclusivamente</b> de la proyección CQRS {@code tramites.vista_tramite}.
 */
@Service
public class ListarTramitesPorDniService implements ListarTramitesPorDniUseCase {

    private final VistaTramiteRepositoryPort vistaTramiteRepositoryPort;

    public ListarTramitesPorDniService(VistaTramiteRepositoryPort vistaTramiteRepositoryPort) {
        this.vistaTramiteRepositoryPort = vistaTramiteRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VistaTramite> listarPorDni(String dni) {
        Dni dniValidado = new Dni(dni);
        return vistaTramiteRepositoryPort.buscarPorDni(dniValidado.valor());
    }
}
