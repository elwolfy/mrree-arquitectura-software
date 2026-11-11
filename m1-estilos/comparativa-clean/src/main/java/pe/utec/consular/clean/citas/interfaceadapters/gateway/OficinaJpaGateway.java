package pe.utec.consular.clean.citas.interfaceadapters.gateway;

import pe.utec.consular.clean.citas.entities.OficinaConsular;
import pe.utec.consular.clean.citas.usecase.OficinaGateway;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del gateway de oficinas consulares en {@code interfaceadapters.gateway}.
 * <p>Concepto: Clean Architecture – Interface Adapter Gateway (Círculo 3, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.adapter.out.persistence.OficinaJpaAdapter}.
 */
public class OficinaJpaGateway implements OficinaGateway {

    private final OficinaPersistenceDataSource dataSource;

    public OficinaJpaGateway(OficinaPersistenceDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<OficinaConsular> listarTodas() {
        return dataSource.listarTodas();
    }

    @Override
    public Optional<OficinaConsular> buscarPorId(String id) {
        return dataSource.buscarPorId(id);
    }

    @Override
    public boolean existe(String id) {
        return dataSource.existe(id);
    }
}

