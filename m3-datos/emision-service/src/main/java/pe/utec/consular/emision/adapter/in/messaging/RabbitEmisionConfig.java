package pe.utec.consular.emision.adapter.in.messaging;

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
 * Configuración de la cola {@code emision.comandos} (routing key {@code emision.solicitar})
 * y su Dead Letter Queue {@code emision.comandos.dlq}.
 */
@Configuration
public class RabbitEmisionConfig {

    public static final String EXCHANGE_EVENTOS = "consular.eventos";
    public static final String EXCHANGE_DLX = "consular.eventos.dlx";
    public static final String COLA_EMISION_COMANDOS = "emision.comandos";
    public static final String COLA_EMISION_COMANDOS_DLQ = "emision.comandos.dlq";
    public static final String RK_EMISION_SOLICITAR = "emision.solicitar";
    public static final String RK_EMISION_COMPLETADA = "emision.completada";
    public static final String RK_EMISION_FALLIDA = "emision.fallida";

    @Bean
    public TopicExchange consularEventosExchange() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    @Bean
    public DirectExchange consularEventosDlx() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue colaEmisionComandos() {
        return QueueBuilder.durable(COLA_EMISION_COMANDOS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_EMISION_COMANDOS_DLQ)
                .build();
    }

    @Bean
    public Queue colaEmisionComandosDlq() {
        return QueueBuilder.durable(COLA_EMISION_COMANDOS_DLQ).build();
    }

    @Bean
    public Binding bindingEmisionComandos(Queue colaEmisionComandos, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaEmisionComandos).to(consularEventosExchange).with(RK_EMISION_SOLICITAR);
    }

    @Bean
    public Binding bindingEmisionComandosDlq(Queue colaEmisionComandosDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaEmisionComandosDlq).to(consularEventosDlx).with(COLA_EMISION_COMANDOS_DLQ);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

