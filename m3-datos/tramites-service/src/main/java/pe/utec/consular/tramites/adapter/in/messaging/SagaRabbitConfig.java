package pe.utec.consular.tramites.adapter.in.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.utec.consular.tramites.adapter.out.messaging.RabbitConfig;

/**
 * Configuración declarativa de las colas del orquestador de la saga en {@code tramites-service}:
 * <ul>
 *   <li>{@code tramites.pago.confirmado} (routing key {@code pago.confirmado})</li>
 *   <li>{@code tramites.emision.completada} (routing key {@code emision.completada})</li>
 *   <li>{@code tramites.emision.fallida} (routing key {@code emision.fallida})</li>
 * </ul>
 */
@Configuration
public class SagaRabbitConfig {

    public static final String EXCHANGE_DLX = "consular.eventos.dlx";

    public static final String COLA_PAGO_CONFIRMADO = "tramites.pago.confirmado";
    public static final String COLA_PAGO_CONFIRMADO_DLQ = "tramites.pago.confirmado.dlq";
    public static final String RK_PAGO_CONFIRMADO = "pago.confirmado";

    public static final String COLA_EMISION_COMPLETADA = "tramites.emision.completada";
    public static final String COLA_EMISION_COMPLETADA_DLQ = "tramites.emision.completada.dlq";
    public static final String RK_EMISION_COMPLETADA = "emision.completada";

    public static final String COLA_EMISION_FALLIDA = "tramites.emision.fallida";
    public static final String COLA_EMISION_FALLIDA_DLQ = "tramites.emision.fallida.dlq";
    public static final String RK_EMISION_FALLIDA = "emision.fallida";

    @Bean
    public DirectExchange consularEventosDlx() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue colaTramitesPagoConfirmado() {
        return QueueBuilder.durable(COLA_PAGO_CONFIRMADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_PAGO_CONFIRMADO_DLQ)
                .build();
    }

    @Bean
    public Queue colaTramitesPagoConfirmadoDlq() {
        return QueueBuilder.durable(COLA_PAGO_CONFIRMADO_DLQ).build();
    }

    @Bean
    public Binding bindingTramitesPagoConfirmado(Queue colaTramitesPagoConfirmado, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaTramitesPagoConfirmado).to(consularEventosExchange).with(RK_PAGO_CONFIRMADO);
    }

    @Bean
    public Binding bindingTramitesPagoConfirmadoDlq(Queue colaTramitesPagoConfirmadoDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaTramitesPagoConfirmadoDlq).to(consularEventosDlx).with(COLA_PAGO_CONFIRMADO_DLQ);
    }

    @Bean
    public Queue colaTramitesEmisionCompletada() {
        return QueueBuilder.durable(COLA_EMISION_COMPLETADA)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_EMISION_COMPLETADA_DLQ)
                .build();
    }

    @Bean
    public Queue colaTramitesEmisionCompletadaDlq() {
        return QueueBuilder.durable(COLA_EMISION_COMPLETADA_DLQ).build();
    }

    @Bean
    public Binding bindingTramitesEmisionCompletada(Queue colaTramitesEmisionCompletada, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaTramitesEmisionCompletada).to(consularEventosExchange).with(RK_EMISION_COMPLETADA);
    }

    @Bean
    public Binding bindingTramitesEmisionCompletadaDlq(Queue colaTramitesEmisionCompletadaDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaTramitesEmisionCompletadaDlq).to(consularEventosDlx).with(COLA_EMISION_COMPLETADA_DLQ);
    }

    @Bean
    public Queue colaTramitesEmisionFallida() {
        return QueueBuilder.durable(COLA_EMISION_FALLIDA)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", COLA_EMISION_FALLIDA_DLQ)
                .build();
    }

    @Bean
    public Queue colaTramitesEmisionFallidaDlq() {
        return QueueBuilder.durable(COLA_EMISION_FALLIDA_DLQ).build();
    }

    @Bean
    public Binding bindingTramitesEmisionFallida(Queue colaTramitesEmisionFallida, TopicExchange consularEventosExchange) {
        return BindingBuilder.bind(colaTramitesEmisionFallida).to(consularEventosExchange).with(RK_EMISION_FALLIDA);
    }

    @Bean
    public Binding bindingTramitesEmisionFallidaDlq(Queue colaTramitesEmisionFallidaDlq, DirectExchange consularEventosDlx) {
        return BindingBuilder.bind(colaTramitesEmisionFallidaDlq).to(consularEventosDlx).with(COLA_EMISION_FALLIDA_DLQ);
    }
}

