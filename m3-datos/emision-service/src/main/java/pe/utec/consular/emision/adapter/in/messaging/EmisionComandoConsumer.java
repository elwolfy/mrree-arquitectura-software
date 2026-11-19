package pe.utec.consular.emision.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pe.utec.consular.emision.application.port.in.ProcesarSolicitudEmisionCommand;
import pe.utec.consular.emision.application.port.in.ProcesarSolicitudEmisionUseCase;

import java.util.UUID;

/**
 * Consumidor RabbitMQ de la cola {@code emision.comandos} (comando {@code emision.solicitar}).
 */
@Component
public class EmisionComandoConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmisionComandoConsumer.class);

    private final ProcesarSolicitudEmisionUseCase procesarSolicitudEmisionUseCase;

    public EmisionComandoConsumer(ProcesarSolicitudEmisionUseCase procesarSolicitudEmisionUseCase) {
        this.procesarSolicitudEmisionUseCase = procesarSolicitudEmisionUseCase;
    }

    @RabbitListener(queues = RabbitEmisionConfig.COLA_EMISION_COMANDOS)
    public void recibirSolicitudEmision(@Payload EmisionSolicitarCommand payload,
                                        @Header(name = "messageId", required = false) String headerMessageId,
                                        Message message) {
        String messageId = resolverMessageId(headerMessageId, message);
        log.info("Recibido comando emision.solicitar para trámite {} (messageId={})",
                payload.tramiteId(), messageId);
        procesarSolicitudEmisionUseCase.procesar(new ProcesarSolicitudEmisionCommand(messageId, payload.tramiteId()));
    }

    private String resolverMessageId(String headerMessageId, Message message) {
        if (headerMessageId != null && !headerMessageId.isBlank()) {
            return headerMessageId;
        }
        if (message != null && message.getMessageProperties().getMessageId() != null) {
            return message.getMessageProperties().getMessageId();
        }
        return UUID.randomUUID().toString();
    }
}

