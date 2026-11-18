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
 * Consumidor de RabbitMQ para la cola {@code notificaciones.tramite.registrado}.
 * <p><b>MODELO DE REFERENCIA PARA LA ASIGNACIÓN A2:</b>
 * Este consumidor ilustra paso a paso los tres pilares de un consumidor resiliente en arquitecturas
 * dirigidas por eventos (sección 2.3 del curso):
 * <ol>
 *   <li><b>Idempotencia por {@code messageId}:</b> RabbitMQ garantiza entrega <i>at-least-once</i>
 *       (al menos una vez), por lo que un mismo mensaje puede entregarse más de una vez tras un
 *       reintento de red. Extraemos el identificador único {@code messageId} de las cabeceras AMQP
 *       y verificamos en {@code notificaciones.mensaje_procesado} dentro de la misma transacción
 *       de base de datos que guarda el {@code Aviso}. Si el {@code messageId} ya existe, el mensaje
 *       se descarta limpiamente con log {@code INFO} sin enviar un segundo correo.</li>
 *   <li><b>Reintentos con espera exponencial (Exponential Backoff):</b> Configurados en
 *       {@code application.yml} ({@code spring.rabbitmq.listener.simple.retry}), realizando
 *       3 reintentos adicionales (4 intentos en total) con intervalos crecientes ({@code 200ms},
 *       {@code 400ms}, {@code 800ms}) ante cualquier excepción transitoria.</li>
 *   <li><b>Cola de mensajes fallidos (Dead Letter Queue – DLQ):</b> Con
 *       {@code default-requeue-rejected: false}, cuando se agotan los reintentos, el broker
 *       encamina automáticamente el mensaje hacia {@code consular.eventos.dlx} con routing key
 *       {@code notificaciones.tramite.registrado.dlq}, evitando ciclos infinitos de reprocesamiento.</li>
 * </ol>
 * <p>Sesión: S3.
 */
@Component
public class TramiteRegistradoConsumer {

    private static final Logger log = LoggerFactory.getLogger(TramiteRegistradoConsumer.class);

    private final ProcesarNotificacionUseCase procesarNotificacionUseCase;

    public TramiteRegistradoConsumer(ProcesarNotificacionUseCase procesarNotificacionUseCase) {
        this.procesarNotificacionUseCase = procesarNotificacionUseCase;
    }

    @RabbitListener(queues = RabbitMessagingConfig.COLA_TRAMITE_REGISTRADO)
    public void consumirTramiteRegistrado(
            @Payload TramiteRegistradoEvent evento,
            @Header(name = "messageId", required = false) String headerMessageId,
            Message rawMessage) {
        UUID messageId = extraerMessageId(headerMessageId, rawMessage);
        log.info("Recibido evento tramite.registrado para trámite {} tipo {} (messageId={})",
                evento.codigo(), evento.tipoTramite(), messageId);

        String asunto = "Registro de trámite consular " + evento.codigo();
        String cuerpo = String.format(
                "Estimado(a) ciudadano(a) con DNI %s,%n%nSu solicitud de trámite %s (%s) ha sido registrada y se encuentra pendiente de pago.",
                evento.dni(), evento.codigo(), evento.tipoTramite()
        );

        procesarNotificacionUseCase.procesarEvento(
                messageId,
                RabbitMessagingConfig.RK_TRAMITE_REGISTRADO,
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

