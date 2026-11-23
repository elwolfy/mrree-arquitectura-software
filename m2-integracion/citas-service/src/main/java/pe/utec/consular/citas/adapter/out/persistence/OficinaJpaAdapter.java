package pe.utec.consular.citas.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA que implementa el puerto secundario de oficinas consulares.
 * <p>Concepto: Adaptador secundario hexagonal (sección 1.3).
 * <p>Sesión: S3.
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

    private OficinaConsular aDominio(OficinaJpaEntity entity) {
        return new OficinaConsular(
                entity.getId(),
                entity.getNombre(),
                entity.getPais(),
                entity.getCiudad(),
                entity.getZonaHoraria()
        );
    }
}

