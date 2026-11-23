package pe.utec.consular.pagos.adapter.out.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.pagos.adapter.out.persistence.OutboxJpaEntity;
import pe.utec.consular.pagos.adapter.out.persistence.SpringDataOutboxRepository;

import java.time.Instant;
import java.util.List;

/**
 * Relay del patrón <b>Transactional Outbox</b> propio de {@code pagos-service} (sin código compartido
 * con {@code tramites-service}), encargado de publicar {@code pago.confirmado} y {@code pago.anulado}
 * desde {@code pagos.outbox} hacia RabbitMQ cada 500 ms.
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

                log.info("Pagos Outbox relay publicó evento {} (id={}, pagoId={}) en exchange {}",
                        tipoEvento, messageId, registro.getAgregadoId(), RabbitConfig.EXCHANGE_EVENTOS);
            } catch (Exception ex) {
                log.warn("Pagos Outbox relay no pudo publicar registro {} (tipo={}); se reintentará: {}",
                        registro.getId(), registro.getTipo(), ex.getMessage());
                break;
            }
        }
        return publicados;
    }
}

