package pe.utec.consular.pagos.adapter.out.outbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pe.utec.consular.pagos.adapter.out.persistence.OutboxJpaEntity;
import pe.utec.consular.pagos.adapter.out.persistence.SpringDataOutboxRepository;
import pe.utec.consular.pagos.application.port.out.OutboxPort;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador que inserta registros en {@code pagos.outbox} dentro de la misma transacción local
 * que persiste el cambio en {@code pagos.pago}.
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

