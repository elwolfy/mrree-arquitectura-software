package pe.utec.consular.tramites.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.out.PublicadorDeEventosPort;
import pe.utec.consular.tramites.domain.TramiteRegistrado;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador de salida que publica el evento {@code tramite.registrado}
 * en RabbitMQ hacia el exchange {@code consular.eventos} con las cabeceras estándar del contrato.
 */
@Component
public class RabbitPublicadorDeEventos implements PublicadorDeEventosPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitPublicadorDeEventos.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitPublicadorDeEventos(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarTramiteRegistrado(TramiteRegistrado evento) {
        publicar(RabbitConfig.ROUTING_KEY_TRAMITE_REGISTRADO, evento, evento.codigo());
    }

    private void publicar(String routingKey, Object cargaUtil, String codigoTramite) {
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
                RabbitConfig.EXCHANGE_EVENTOS,
                routingKey,
                cargaUtil,
                postProcessor
        );

        log.info("Evento {} publicado en exchange {} para trámite {} (messageId={})",
                routingKey, RabbitConfig.EXCHANGE_EVENTOS, codigoTramite, messageId);
    }
}
