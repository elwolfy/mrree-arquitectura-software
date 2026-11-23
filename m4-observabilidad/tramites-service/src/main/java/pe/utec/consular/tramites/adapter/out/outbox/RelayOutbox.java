package pe.utec.consular.tramites.adapter.out.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.adapter.out.messaging.RabbitConfig;
import pe.utec.consular.tramites.adapter.out.persistence.OutboxJpaEntity;
import pe.utec.consular.tramites.adapter.out.persistence.SpringDataOutboxRepository;

import java.time.Instant;
import java.util.List;

/**
 * Relay del patrón <b>Transactional Outbox</b> que publica cada 500 ms los mensajes pendientes
 * de {@code tramites.outbox} hacia RabbitMQ y marca {@code publicado_en}.
 *
 * <h3>¿Qué problema de escritura doble (Dual Write) evita el Transactional Outbox?</h3>
 * <p>Si un caso de uso realiza un {@code COMMIT} en PostgreSQL y acto seguido intenta publicar
 * un mensaje en RabbitMQ sin transacción distribuida (2PC), existen dos ventanas de inconsistencia:
 * <ul>
 *   <li>Si la base de datos confirma la escritura pero el broker o la red fallan justo después,
 *       el trámite queda registrado en PostgreSQL pero el evento {@code tramite.registrado} (o un comando
 *       de la saga) jamás llega a RabbitMQ, dejando a los demás microservicios desincronizados.</li>
 *   <li>Si primero se publica en RabbitMQ y luego la transacción de base de datos hace {@code ROLLBACK},
 *       los consumidores procesan un evento fantasma de un trámite que nunca existió.</li>
 * </ul>
 * <p>Con <b>Transactional Outbox</b>, el caso de uso solo escribe en PostgreSQL dentro de una única
 * transacción ACID local ({@code eventos_tramite} + {@code vista_tramite} + {@code outbox}). Este
 * componente {@code RelayOutbox} sondea periódicamente ({@code @Scheduled}, cada 500 ms) las filas
 * con {@code publicado_en IS NULL}, las envía a RabbitMQ con garantía <i>at-least-once</i> (usando
 * el {@code id} de outbox como {@code messageId} para que los consumidores apliquen idempotencia) y
 * actualiza {@code publicado_en}.
 *
 * <h3>Alternativa industrial: Change Data Capture (CDC / Debezium)</h3>
 * <p>En sistemas de alto caudal donde el sondeo por {@code SELECT} cada pocos milisegundos añade carga
 * sobre la base de datos, el relay por <i>polling</i> puede reemplazarse por un conector de
 * <b>Change Data Capture (CDC)</b> como <b>Debezium</b>, que lee directamente el log de transacciones
 * de PostgreSQL (WAL / <i>Write-Ahead Log</i>) y transmite cada {@code INSERT} de la tabla {@code outbox}
 * hacia el broker en tiempo real sin ejecutar consultas de sondeo.
 *
 * <p>Sesión: S4 y S5.
 */
@Component
public class RelayOutbox {

    private static final Logger log = LoggerFactory.getLogger(RelayOutbox.class);

    private final SpringDataOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public RelayOutbox(SpringDataOutboxRepository outboxRepository,
                       RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.intervalo-ms:500}")
    @Transactional
    public int publicarPendientes() {
        List<OutboxJpaEntity> pendientes = outboxRepository.findByPublicadoEnIsNullOrderByCreadoEnAsc();
        if (pendientes.isEmpty()) {
            return 0;
        }

        int publicados = 0;
        for (OutboxJpaEntity registro : pendientes) {
            try {
                String messageId = registro.getId().toString();
                String ocurridoEn = registro.getCreadoEn().toString();
                String tipoEvento = registro.getTipo();
                String traceparentGuardado = registro.getTraceparent();

                MessagePostProcessor postProcessor = message -> {
                    message.getMessageProperties().setMessageId(messageId);
                    message.getMessageProperties().setHeader("messageId", messageId);
                    message.getMessageProperties().setHeader("tipoEvento", tipoEvento);
                    message.getMessageProperties().setHeader("ocurridoEn", ocurridoEn);
                    if (traceparentGuardado != null && !traceparentGuardado.isBlank()) {
                        message.getMessageProperties().setHeader("traceparent", traceparentGuardado);
                    }
                    return message;
                };

                rabbitTemplate.convertAndSend(
                        RabbitConfig.EXCHANGE_EVENTOS,
                        tipoEvento,
                        registro.getCarga(),
                        postProcessor
                );

                registro.marcarPublicado(Instant.now());
                outboxRepository.save(registro);
                publicados++;

                log.info("Outbox relay publicó evento {} (id={}, agregadoId={}) en exchange {}",
                        tipoEvento, messageId, registro.getAgregadoId(), RabbitConfig.EXCHANGE_EVENTOS);
            } catch (Exception ex) {
                log.warn("Outbox relay no pudo publicar registro {} (tipo={}); permanecerá pendiente en tramites.outbox para reintento: {}",
                        registro.getId(), registro.getTipo(), ex.getMessage());
                break;
            }
        }
        return publicados;
    }
}

