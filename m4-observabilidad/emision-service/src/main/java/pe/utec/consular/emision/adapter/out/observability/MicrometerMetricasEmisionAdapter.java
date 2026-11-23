package pe.utec.consular.emision.adapter.out.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import pe.utec.consular.emision.application.port.out.MetricasEmisionPort;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Adaptador que registra el histograma {@code consular_emision_duracion_seconds} en Micrometer.
 * <p><b>MODELO DE REFERENCIA PARA EL PROYECTO FINAL:</b>
 * Ilustra cómo configurar un {@link Timer} con {@code publishPercentileHistogram()} y cubetas SLO
 * ({@code serviceLevelObjectives}) para calcular quantiles p50, p95 y p99 con {@code histogram_quantile()}
 * en PromQL dentro del tablero <i>"Flujo de trámite"</i> de Grafana.
 * <p>Concepto: Observabilidad – Histogramas en Micrometer / Prometheus (sección 4.2 del plan).
 * <p>Sesión: S6.
 */
@Component
public class MicrometerMetricasEmisionAdapter implements MetricasEmisionPort {

    public static final String METRICA_EMISION_DURACION = "consular_emision_duracion";

    private final Timer emisionDuracionTimer;

    public MicrometerMetricasEmisionAdapter(MeterRegistry meterRegistry) {
        this.emisionDuracionTimer = Timer.builder(METRICA_EMISION_DURACION)
                .description("Duración del proceso de generación y firma electrónica del documento consular")
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                        Duration.ofMillis(100),
                        Duration.ofMillis(250),
                        Duration.ofMillis(500),
                        Duration.ofMillis(750),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(5)
                )
                .register(meterRegistry);
    }

    @Override
    public void registrarDuracionEmisionNanos(long duracionNanos) {
        emisionDuracionTimer.record(duracionNanos, TimeUnit.NANOSECONDS);
    }
}

