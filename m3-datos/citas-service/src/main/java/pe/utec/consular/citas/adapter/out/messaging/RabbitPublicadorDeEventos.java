package pe.utec.consular.citas.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.utec.consular.citas.application.port.out.PublicadorDeEventosPort;
import pe.utec.consular.citas.domain.CitaReservada;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador de salida que publica eventos de integración en RabbitMQ hacia el exchange {@code consular.eventos}.
 * <p>Concepto: Adaptador secundario de mensajería y contrato de eventos con metadatos (secciones 1.3 y 6.1).
 * Agrega las cabeceras estándar del contrato: {@code messageId}, {@code tipoEvento} y {@code ocurridoEn}.
 * <p>Sesión: S3.
 */
@Component
public class RabbitPublicadorDeEventos implements PublicadorDeEventosPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitPublicadorDeEventos.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitPublicadorDeEventos(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarCitaReservada(CitaReservada evento) {
        String messageId = UUID.randomUUID().toString();
        String tipoEvento = RabbitConfig.ROUTING_KEY_CITA_RESERVADA;
        String ocurridoEn = Instant.now().toString();

        MessagePostProcessor postProcessor = message -> {
            message.getMessageProperties().setMessageId(messageId);
            message.getMessageProperties().setHeader("messageId", messageId);
            message.getMessageProperties().setHeader("tipoEvento", tipoEvento);
            message.getMessageProperties().setHeader("ocurridoEn", ocurridoEn);
            return message;
        };

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_EVENTOS,
                RabbitConfig.ROUTING_KEY_CITA_RESERVADA,
                evento,
                postProcessor
        );

        log.info("Evento {} publicado en exchange {} para cita {} (messageId={})",
                tipoEvento, RabbitConfig.EXCHANGE_EVENTOS, evento.codigo(), messageId);
    }
}

