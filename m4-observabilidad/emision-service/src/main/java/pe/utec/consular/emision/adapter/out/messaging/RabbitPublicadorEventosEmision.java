package pe.utec.consular.emision.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.utec.consular.emision.adapter.in.messaging.RabbitEmisionConfig;
import pe.utec.consular.emision.application.port.out.PublicadorEventosEmisionPort;
import pe.utec.consular.emision.domain.EmisionCompletada;
import pe.utec.consular.emision.domain.EmisionFallida;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador RabbitMQ que publica {@code emision.completada} o {@code emision.fallida}
 * hacia el exchange {@code consular.eventos}.
 */
@Component
public class RabbitPublicadorEventosEmision implements PublicadorEventosEmisionPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitPublicadorEventosEmision.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitPublicadorEventosEmision(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarEmisionCompletada(EmisionCompletada evento) {
        publicar(RabbitEmisionConfig.RK_EMISION_COMPLETADA, evento, evento.tramiteId());
    }

    @Override
    public void publicarEmisionFallida(EmisionFallida evento) {
        publicar(RabbitEmisionConfig.RK_EMISION_FALLIDA, evento, evento.tramiteId());
    }

    private void publicar(String routingKey, Object cargaUtil, UUID tramiteId) {
        String messageId = UUID.randomUUID().toString();
        String ocurridoEn = Instant.now().toString();

        MessagePostProcessor postProcessor = message -> {
            message.getMessageProperties().setMessageId(messageId);
            message.getMessageProperties().setHeader("messageId", messageId);
            message.getMessageProperties().setHeader("tipoEvento", routingKey);
            message.getMessageProperties().setHeader("ocurridoEn", ocurridoEn);
            return message;
        };

        rabbitTemplate.convertAndSend(
                RabbitEmisionConfig.EXCHANGE_EVENTOS,
                routingKey,
                cargaUtil,
                postProcessor
        );

        log.info("Evento {} publicado en exchange {} para trámite {} (messageId={})",
                routingKey, RabbitEmisionConfig.EXCHANGE_EVENTOS, tramiteId, messageId);
    }
}

