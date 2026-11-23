package pe.utec.consular.pagos.adapter.out.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración de RabbitMQ y programación de tareas ({@code @EnableScheduling}) para el Outbox de {@code pagos-service}.
 */
@Configuration
@EnableScheduling
public class RabbitConfig {

    public static final String EXCHANGE_EVENTOS = "consular.eventos";

    @Bean
    public TopicExchange consularEventosExchange() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

