package pe.utec.consular.tramites.application.port.out;

import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario para el modelo de lectura CQRS ({@code tramites.vista_tramite}).
 */
public interface VistaTramiteRepositoryPort {

    VistaTramite guardar(VistaTramite vista);

    Optional<VistaTramite> buscarPorId(UUID tramiteId);

    List<VistaTramite> buscarPorDni(String dni);

    List<VistaTramite> buscarPorPaisDestinoEnExpediente(String paisDestino);

    void eliminarTodas();
}

