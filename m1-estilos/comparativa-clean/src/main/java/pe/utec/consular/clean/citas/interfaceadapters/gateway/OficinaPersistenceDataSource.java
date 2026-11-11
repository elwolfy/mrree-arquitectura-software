package pe.utec.consular.clean.citas.interfaceadapters.gateway;

import pe.utec.consular.clean.citas.entities.OficinaConsular;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de oficinas consulares que {@code frameworks.persistence} implementa para {@link OficinaJpaGateway}.
 */
public interface OficinaPersistenceDataSource {

    List<OficinaConsular> listarTodas();

    Optional<OficinaConsular> buscarPorId(String id);

    boolean existe(String id);
}

