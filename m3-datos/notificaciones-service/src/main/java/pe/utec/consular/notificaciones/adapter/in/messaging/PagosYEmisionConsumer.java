package pe.utec.consular.notificaciones.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pe.utec.consular.notificaciones.application.port.in.ProcesarNotificacionUseCase;

import java.util.UUID;

/**
 * Consumidor idempotente de RabbitMQ para {@code pago.confirmado}, {@code pago.anulado}
 * y {@code emision.completada} en {@code notificaciones-service} (M3).
 */
@Component
public class PagosYEmisionConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagosYEmisionConsumer.class);

    private final ProcesarNotificacionUseCase procesarNotificacionUseCase;

    public PagosYEmisionConsumer(ProcesarNotificacionUseCase procesarNotificacionUseCase) {
        this.procesarNotificacionUseCase = procesarNotificacionUseCase;
    }

    @RabbitListener(queues = RabbitMessagingConfig.COLA_PAGO_CONFIRMADO)
    public void consumirPagoConfirmado(@Payload PagoConfirmadoEvent evento,
                                       @Header(name = "messageId", required = false) String headerMessageId,
                                       Message rawMessage) {
        UUID messageId = extraerMessageId(headerMessageId, rawMessage);
        log.info("Recibido evento pago.confirmado para pago {} del trámite {} (messageId={})",
                evento.pagoId(), evento.tramiteId(), messageId);

        String asunto = "Constancia de pago confirmado - Trámite " + evento.tramiteId();
        String cuerpo = String.format(
                "Su pago %s por %s PEN asociado al trámite %s ha sido confirmado exitosamente.",
                evento.pagoId(), evento.monto(), evento.tramiteId()
        );

        procesarNotificacionUseCase.procesarEvento(
                messageId,
                RabbitMessagingConfig.RK_PAGO_CONFIRMADO,
                "00000000",
                "ciudadano@correo.lab",
                asunto,
                cuerpo,
                evento.tramiteId().toString()
        );
    }

    @RabbitListener(queues = RabbitMessagingConfig.COLA_PAGO_ANULADO)
    public void consumirPagoAnulado(@Payload PagoAnuladoEvent evento,
                                    @Header(name = "messageId", required = false) String headerMessageId,
                                    Message rawMessage) {
        UUID messageId = extraerMessageId(headerMessageId, rawMessage);
        log.info("Recibido evento pago.anulado para pago {} del trámite {} (messageId={})",
                evento.pagoId(), evento.tramiteId(), messageId);

        String asunto = "Aviso de anulación de pago - Trámite " + evento.tramiteId();
        String cuerpo = String.format(
                "El pago %s asociado al trámite %s ha sido ANULADO. Motivo: %s",
                evento.pagoId(), evento.tramiteId(), evento.motivo()
        );

        procesarNotificacionUseCase.procesarEvento(
                messageId,
                RabbitMessagingConfig.RK_PAGO_ANULADO,
                "00000000",
                "ciudadano@correo.lab",
                asunto,
                cuerpo,
                evento.tramiteId().toString()
        );
    }

    @RabbitListener(queues = RabbitMessagingConfig.COLA_EMISION_COMPLETADA)
    public void consumirEmisionCompletada(@Payload EmisionCompletadaEvent evento,
                                          @Header(name = "messageId", required = false) String headerMessageId,
                                          Message rawMessage) {
        UUID messageId = extraerMessageId(headerMessageId, rawMessage);
        log.info("Recibido evento emision.completada para trámite {} documento {} (messageId={})",
                evento.tramiteId(), evento.numeroDocumento(), messageId);

        String asunto = "Documento consular emitido " + evento.numeroDocumento();
        String cuerpo = String.format(
                "Su trámite %s ha culminado satisfactoriamente. Documento emitido: %s",
                evento.tramiteId(), evento.numeroDocumento()
        );

        procesarNotificacionUseCase.procesarEvento(
                messageId,
                RabbitMessagingConfig.RK_EMISION_COMPLETADA,
                "00000000",
                "ciudadano@correo.lab",
                asunto,
                cuerpo,
                evento.numeroDocumento()
        );
    }

    private UUID extraerMessageId(String headerMessageId, Message rawMessage) {
        String candidate = headerMessageId;
        if ((candidate == null || candidate.isBlank()) && rawMessage != null && rawMessage.getMessageProperties() != null) {
            candidate = rawMessage.getMessageProperties().getMessageId();
        }
        if (candidate != null && !candidate.isBlank()) {
            try {
                return UUID.fromString(candidate);
            } catch (IllegalArgumentException ignored) {
                return UUID.nameUUIDFromBytes(candidate.getBytes());
            }
        }
        return UUID.randomUUID();
    }
}

