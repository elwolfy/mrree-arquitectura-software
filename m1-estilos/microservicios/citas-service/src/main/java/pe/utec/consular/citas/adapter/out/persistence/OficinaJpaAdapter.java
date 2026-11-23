package pe.utec.consular.citas.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA para el puerto de salida de oficinas consulares.
 */
@Component
public class OficinaJpaAdapter implements OficinaRepositoryPort {

    private final SpringDataOficinaRepository repository;

    public OficinaJpaAdapter(SpringDataOficinaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<OficinaConsular> listarTodas() {
        return repository.findAll().stream()
                .map(this::aDominio)
                .toList();
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
        return new OficinaConsular(e.getId(), e.getNombre(), e.getPais(), e.getCiudad(), e.getZonaHoraria());
    }
}

