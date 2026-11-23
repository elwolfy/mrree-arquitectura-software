package pe.utec.consular.citas.application.port.out;

import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la consulta de oficinas consulares.
 */
public interface OficinaRepositoryPort {

    List<OficinaConsular> listarTodas();

    Optional<OficinaConsular> buscarPorId(String id);

    boolean existe(String id);
}

