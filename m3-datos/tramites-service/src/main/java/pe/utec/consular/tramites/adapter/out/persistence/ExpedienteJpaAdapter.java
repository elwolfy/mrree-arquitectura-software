package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.out.ExpedienteRepositoryPort;

import java.util.Map;
import java.util.UUID;

/**
 * Adaptador JPA para almacenar la ficha documental JSONB del expediente ({@code tramites.expediente}).
 */
@Component
public class ExpedienteJpaAdapter implements ExpedienteRepositoryPort {

    private final SpringDataExpedienteRepository repository;

    public ExpedienteJpaAdapter(SpringDataExpedienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardarExpediente(UUID tramiteId, Map<String, Object> metadatos) {
        repository.save(new ExpedienteJpaEntity(tramiteId, metadatos));
    }
}

