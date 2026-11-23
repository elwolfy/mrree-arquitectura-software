package pe.utec.consular.citas.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ y serialización JSON para eventos de integración en citas-service.
 * <p>Concepto: Mensajería asíncrona y serialización JSON de eventos (regla 02 y sección 6.1 del contrato).
 * <p>Sesión: S3.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_EVENTOS = "consular.eventos";
    public static final String ROUTING_KEY_CITA_RESERVADA = "cita.reservada";

    @Bean
    public TopicExchange consularEventosExchange() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

