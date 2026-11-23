package pe.utec.consular.pagos.adapter.out.outbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import pe.utec.consular.pagos.adapter.out.persistence.OutboxJpaEntity;
import pe.utec.consular.pagos.adapter.out.persistence.SpringDataOutboxRepository;
import pe.utec.consular.pagos.application.port.out.OutboxPort;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador que inserta registros en {@code pagos.outbox} dentro de la misma transacción local
 * que persiste el cambio en {@code pagos.pago}, preservando la cabecera W3C {@code traceparent}.
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
                construirTraceparentActual(),
                Instant.now(),
                null
        );
        outboxRepository.save(entidad);
    }

    private String construirTraceparentActual() {
        String traceId = MDC.get("trace_id");
        String spanId = MDC.get("span_id");
        if (traceId != null && traceId.length() == 32 && spanId != null && spanId.length() == 16) {
            String flags = MDC.get("trace_flags");
            if (flags == null || flags.length() != 2) {
                flags = "01";
            }
            return "00-" + traceId + "-" + spanId + "-" + flags;
        }
        return null;
    }
}

