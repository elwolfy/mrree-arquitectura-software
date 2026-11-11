package pe.utec.consular.clean.citas.frameworks.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.clean.citas.entities.OficinaConsular;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.OficinaPersistenceDataSource;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA del origen de datos de oficinas consulares en {@code frameworks.persistence}.
 */
@Component
public class OficinaJpaPersistenceDataSource implements OficinaPersistenceDataSource {

    private final SpringDataOficinaRepository repository;

    public OficinaJpaPersistenceDataSource(SpringDataOficinaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<OficinaConsular> listarTodas() {
        return repository.findAll().stream().map(this::aDominio).toList();
    }

    @Override
    public Optional<OficinaConsular> buscarPorId(String id) {
        return repository.findById(id).map(this::aDominio);
    }

    @Override
    public boolean existe(String id) {
        return repository.existsById(id);
    }

    private OficinaConsular aDominio(OficinaJpaEntity e) {
        return new OficinaConsular(
                e.getId(),
                e.getNombre(),
                e.getPais(),
                e.getCiudad(),
                e.getZonaHoraria()
        );
    }
}

