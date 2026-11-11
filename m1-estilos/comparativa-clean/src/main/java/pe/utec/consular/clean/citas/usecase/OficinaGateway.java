package pe.utec.consular.clean.citas.usecase;

import pe.utec.consular.clean.citas.entities.OficinaConsular;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso al catálogo de oficinas consulares requerida por los casos de uso.
 * <p>Concepto: Clean Architecture – Círculo 2 ({@code usecase}, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code OficinaRepositoryPort} en {@code monolito-modular}.
 */
public interface OficinaGateway {

    List<OficinaConsular> listarTodas();

    Optional<OficinaConsular> buscarPorId(String id);

    boolean existe(String id);
}

