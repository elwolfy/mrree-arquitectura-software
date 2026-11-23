package pe.utec.consular.monolito.citas.application.port.out;

import pe.utec.consular.monolito.citas.domain.OficinaConsular;

import java.util.List;
import java.util.Optional;

/**
 * Puerto secundario para acceso al catálogo de oficinas consulares.
 * <p>Concepto: Arquitectura hexagonal – puerto de salida (sección 1.3).
 */
public interface OficinaRepositoryPort {

    List<OficinaConsular> listarTodas();

    Optional<OficinaConsular> buscarPorId(String id);

    boolean existe(String id);
}
