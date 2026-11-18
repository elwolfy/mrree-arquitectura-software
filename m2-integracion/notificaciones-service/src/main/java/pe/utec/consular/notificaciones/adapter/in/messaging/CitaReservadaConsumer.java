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
 * Consumidor de RabbitMQ para la cola {@code notificaciones.cita.reservada}.
 * <p>Concepto: Patrón Observer distribuido entre microservicios (sección 2.1 y 2.3).
 */
@Component
public class CitaReservadaConsumer {

    private static final Logger log = LoggerFactory.getLogger(CitaReservadaConsumer.class);

    private final ProcesarNotificacionUseCase procesarNotificacionUseCase;

    public CitaReservadaConsumer(ProcesarNotificacionUseCase procesarNotificacionUseCase) {
        this.procesarNotificacionUseCase = procesarNotificacionUseCase;
    }

    @RabbitListener(queues = RabbitMessagingConfig.COLA_CITA_RESERVADA)
    public void consumirCitaReservada(
            @Payload CitaReservadaEvent evento,
            @Header(name = "messageId", required = false) String headerMessageId,
            Message rawMessage) {
        UUID messageId = extraerMessageId(headerMessageId, rawMessage);
        log.info("Recibido evento cita.reservada para código {} (messageId={})", evento.codigo(), messageId);

        String asunto = "Confirmación de cita consular " + evento.codigo();
        String cuerpo = String.format(
                "Estimado(a) ciudadano(a) con DNI %s,%n%nSu cita %s en la oficina consular %s ha sido confirmada para el %s a las %s.",
                evento.dni(), evento.codigo(), evento.oficinaId(), evento.fecha(), evento.hora()
        );

        procesarNotificacionUseCase.procesarEvento(
                messageId,
                RabbitMessagingConfig.RK_CITA_RESERVADA,
                evento.dni(),
                evento.email(),
                asunto,
                cuerpo,
                evento.codigo()
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

