package pe.utec.consular.tramites.adapter.out.outbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.adapter.out.persistence.OutboxJpaEntity;
import pe.utec.consular.tramites.adapter.out.persistence.SpringDataOutboxRepository;
import pe.utec.consular.tramites.application.port.out.OutboxPort;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador que persiste registros en {@code tramites.outbox} dentro de la misma
 * transacción local que guarda los eventos de dominio en {@code tramites.eventos_tramite}.
 */
@Component
public class OutboxPersistenceAdapter implements OutboxPort {

    private final SpringDataOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxPersistenceAdapter(SpringDataOutboxRepository outboxRepository,
                                    ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registrarEnOutbox(UUID agregadoId, String tipoEvento, Object cargaUtil) {
        Map<String, Object> mapaCarga = objectMapper.convertValue(cargaUtil, new TypeReference<>() {});
        OutboxJpaEntity entidad = new OutboxJpaEntity(
                UUID.randomUUID(),
                agregadoId,
                tipoEvento,
                mapaCarga,
                Instant.now(),
                null
        );
        outboxRepository.save(entidad);
    }
}

