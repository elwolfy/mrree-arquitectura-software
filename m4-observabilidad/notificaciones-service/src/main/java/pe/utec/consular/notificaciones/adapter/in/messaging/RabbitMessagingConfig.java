package pe.utec.consular.notificaciones.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración declarativa de colas, exchanges y Dead Letter Queues (DLQ) en RabbitMQ
 * para {@code notificaciones-service} en M3.
 */
@Configuration
public class RabbitMessagingConfig {

    public static final String EXCHANGE_EVENTOS = "consular.eventos";
    public static final String EXCHANGE_DLX = "consular.eventos.dlx";

    public static final String COLA_CITA_RESERVADA = "notificaciones.cita.reservada";
    public static final String COLA_CITA_RESERVADA_DLQ = "notificaciones.cita.reservada.dlq";
    public static final String RK_CITA_RESERVADA = "cita.reservada";

    public static final String COLA_TRAMITE_REGISTRADO = "notificaciones.tramite.registrado";
    public static final String COLA_TRAMITE_REGISTRADO_DLQ = "notificaciones.tramite.registrado.dlq";
    public static final String RK_TRAMITE_REGISTRADO = "tramite.registrado";

    public static final String COLA_PAGO_CONFIRMADO = "notificaciones.pago.confirmado";
    public static final String COLA_PAGO_CONFIRMADO_DLQ = "notificaciones.pago.confirmado.dlq";
    public static final String RK_PAGO_CONFIRMADO = "pago.confirmado";

    public static final String COLA_PAGO_ANULADO = "notificaciones.pago.anulado";
    public static final String COLA_PAGO_ANULADO_DLQ = "notificaciones.pago.anulado.dlq";
    public static final String RK_PAGO_ANULADO = "pago.anulado";

    public static final String COLA_EMISION_COMPLETADA = "notificaciones.emision.completada";
    public static final String COLA_EMISION_COMPLETADA_DLQ = "notificaciones.emision.completada.dlq";
    public static final String RK_EMISION_COMPLETADA = "emision.completada";

    @Bean
    public TopicExchange consularEventosExchange() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    @Bean
    public DirectExchange consularEventosDlx() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue colaCitaReservada() {
        return QueueBuilder.durable(COLA_CITA_RESERVADA)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_CITA_RESERVADA_DLQ)
                .build();
    }

    @Bean
    public Queue colaCitaReservadaDlq() {
        return QueueBuilder.durable(COLA_CITA_RESERVADA_DLQ).build();
    }

    @Bean
    public Binding bindingCitaReservada(Queue colaCitaReservada, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaCitaReservada).to(consularEventosExchange).with(RK_CITA_RESERVADA);
    }

    @Bean
    public Binding bindingCitaReservadaDlq(Queue colaCitaReservadaDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaCitaReservadaDlq).to(consularEventosDlx).with(COLA_CITA_RESERVADA_DLQ);
    }

    @Bean
    public Queue colaTramiteRegistrado() {
        return QueueBuilder.durable(COLA_TRAMITE_REGISTRADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_TRAMITE_REGISTRADO_DLQ)
                .build();
    }

    @Bean
    public Queue colaTramiteRegistradoDlq() {
        return QueueBuilder.durable(COLA_TRAMITE_REGISTRADO_DLQ).build();
    }

    @Bean
    public Binding bindingTramiteRegistrado(Queue colaTramiteRegistrado, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaTramiteRegistrado).to(consularEventosExchange).with(RK_TRAMITE_REGISTRADO);
    }

    @Bean
    public Binding bindingTramiteRegistradoDlq(Queue colaTramiteRegistradoDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaTramiteRegistradoDlq).to(consularEventosDlx).with(COLA_TRAMITE_REGISTRADO_DLQ);
    }

    @Bean
    public Queue colaPagoConfirmado() {
        return QueueBuilder.durable(COLA_PAGO_CONFIRMADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_PAGO_CONFIRMADO_DLQ)
                .build();
    }

    @Bean
    public Queue colaPagoConfirmadoDlq() {
        return QueueBuilder.durable(COLA_PAGO_CONFIRMADO_DLQ).build();
    }

    @Bean
    public Binding bindingPagoConfirmado(Queue colaPagoConfirmado, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaPagoConfirmado).to(consularEventosExchange).with(RK_PAGO_CONFIRMADO);
    }

    @Bean
    public Binding bindingPagoConfirmadoDlq(Queue colaPagoConfirmadoDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaPagoConfirmadoDlq).to(consularEventosDlx).with(COLA_PAGO_CONFIRMADO_DLQ);
    }

    @Bean
    public Queue colaPagoAnulado() {
        return QueueBuilder.durable(COLA_PAGO_ANULADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_PAGO_ANULADO_DLQ)
                .build();
    }

    @Bean
    public Queue colaPagoAnuladoDlq() {
        return QueueBuilder.durable(COLA_PAGO_ANULADO_DLQ).build();
    }

    @Bean
    public Binding bindingPagoAnulado(Queue colaPagoAnulado, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaPagoAnulado).to(consularEventosExchange).with(RK_PAGO_ANULADO);
    }

    @Bean
    public Binding bindingPagoAnuladoDlq(Queue colaPagoAnuladoDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaPagoAnuladoDlq).to(consularEventosDlx).with(COLA_PAGO_ANULADO_DLQ);
    }

    @Bean
    public Queue colaEmisionCompletada() {
        return QueueBuilder.durable(COLA_EMISION_COMPLETADA)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_EMISION_COMPLETADA_DLQ)
                .build();
    }

    @Bean
    public Queue colaEmisionCompletadaDlq() {
        return QueueBuilder.durable(COLA_EMISION_COMPLETADA_DLQ).build();
    }

    @Bean
    public Binding bindingEmisionCompletada(Queue colaEmisionCompletada, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaEmisionCompletada).to(consularEventosExchange).with(RK_EMISION_COMPLETADA);
    }

    @Bean
    public Binding bindingEmisionCompletadaDlq(Queue colaEmisionCompletadaDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaEmisionCompletadaDlq).to(consularEventosDlx).with(COLA_EMISION_COMPLETADA_DLQ);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
