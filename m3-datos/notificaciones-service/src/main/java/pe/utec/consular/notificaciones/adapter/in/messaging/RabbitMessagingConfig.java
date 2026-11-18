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
 * para {@code notificaciones-service}.
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
    public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
