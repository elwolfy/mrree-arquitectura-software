package pe.utec.consular.tramites.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.saga.SagaSolicitudTramite;
import pe.utec.consular.tramites.domain.EmisionCompletada;
import pe.utec.consular.tramites.domain.EmisionFallida;
import pe.utec.consular.tramites.domain.PagoConfirmado;

/**
 * Adaptador primario de mensajería que recibe los eventos de respuesta de los participantes
 * de la saga ({@code pago.confirmado}, {@code emision.completada}, {@code emision.fallida})
 * y los delega al orquestador {@link SagaSolicitudTramite}.
 */
@Component
public class SagaEventosConsumer {

    private static final Logger log = LoggerFactory.getLogger(SagaEventosConsumer.class);

    private final SagaSolicitudTramite sagaSolicitudTramite;

    public SagaEventosConsumer(SagaSolicitudTramite sagaSolicitudTramite) {
        this.sagaSolicitudTramite = sagaSolicitudTramite;
    }

    @RabbitListener(queues = SagaRabbitConfig.COLA_PAGO_CONFIRMADO)
    public void recibirPagoConfirmado(@Payload PagoConfirmado evento) {
        log.info("SagaEventosConsumer recibió pago.confirmado: pagoId={}, tramiteId={}",
                evento.pagoId(), evento.tramiteId());
        sagaSolicitudTramite.alConfirmarPago(evento);
    }

    @RabbitListener(queues = SagaRabbitConfig.COLA_EMISION_COMPLETADA)
    public void recibirEmisionCompletada(@Payload EmisionCompletada evento) {
        log.info("SagaEventosConsumer recibió emision.completada: tramiteId={}, numeroDocumento={}",
                evento.tramiteId(), evento.numeroDocumento());
        sagaSolicitudTramite.alCompletarEmision(evento);
    }

    @RabbitListener(queues = SagaRabbitConfig.COLA_EMISION_FALLIDA)
    public void recibirEmisionFallida(@Payload EmisionFallida evento) {
        log.info("SagaEventosConsumer recibió emision.fallida: tramiteId={}, motivo={}",
                evento.tramiteId(), evento.motivo());
        sagaSolicitudTramite.alFallarEmision(evento);
    }
}

