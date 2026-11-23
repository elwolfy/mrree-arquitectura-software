package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.out.SagaInstanciaRepositoryPort;
import pe.utec.consular.tramites.domain.SagaInstancia;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para {@link SagaInstanciaRepositoryPort}.
 */
@Component
public class SagaInstanciaJpaAdapter implements SagaInstanciaRepositoryPort {

    private final SpringDataSagaInstanciaRepository repository;

    public SagaInstanciaJpaAdapter(SpringDataSagaInstanciaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SagaInstancia guardar(SagaInstancia instancia) {
        SagaInstanciaJpaEntity entity = new SagaInstanciaJpaEntity(
                instancia.tramiteId(),
                instancia.paso(),
                instancia.estado(),
                instancia.actualizadoEn()
        );
        SagaInstanciaJpaEntity guardada = repository.saveAndFlush(entity);
        return aDominio(guardada);
    }

    @Override
    public Optional<SagaInstancia> buscarPorTramiteId(UUID tramiteId) {
        return repository.findById(tramiteId).map(this::aDominio);
    }

    private SagaInstancia aDominio(SagaInstanciaJpaEntity entity) {
        return new SagaInstancia(
                entity.getTramiteId(),
                entity.getPaso(),
                entity.getEstado(),
                entity.getActualizadoEn()
        );
    }
}

