package pe.utec.consular.citas.application.port.out;

import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para consultar las oficinas consulares registradas.
 * <p>Concepto: Arquitectura hexagonal – puerto secundario (sección 1.3).
 * <p>Sesión: S3.
 */
public interface OficinaRepositoryPort {

    List<OficinaConsular> listarTodas();

    Optional<OficinaConsular> buscarPorId(String id);

    boolean existe(String id);
}

