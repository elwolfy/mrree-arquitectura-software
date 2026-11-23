package pe.utec.consular.tramites.adapter.out.outbox;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita la ejecución periódica ({@code @Scheduled}) para {@link RelayOutbox}.
 */
@Configuration
@EnableScheduling
public class OutboxSchedulingConfig {
}

