package pe.utec.consular.tramites.application.port.out;

import pe.utec.consular.tramites.domain.Tramite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario para operaciones de persistencia sobre trámites consulares.
 */
public interface TramiteRepositoryPort {

    Tramite guardar(Tramite tramite);

    Optional<Tramite> buscarPorId(UUID id);

    List<Tramite> buscarPorDni(String dni);

    long siguienteSecuencia();
}

