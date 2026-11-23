package pe.utec.consular.tramites.adapter.out.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.adapter.out.persistence.SpringDataVistaTramiteRepository;
import pe.utec.consular.tramites.application.port.out.MetricasTramitePort;
import pe.utec.consular.tramites.domain.EstadoTramite;
import pe.utec.consular.tramites.domain.TipoTramite;

import java.util.EnumMap;
import java.util.Map;

/**
 * Adaptador de observabilidad que publica en Micrometer las métricas de negocio de {@code tramites-service}:
 * <ul>
 *   <li>{@code consular_tramites_registrados_total{tipo}}: contador acumulado de trámites iniciados por tipo
 *       ({@code LEGALIZACION}, {@code APOSTILLA}, {@code REGISTRO_CONSULAR}).</li>
 *   <li>{@code consular_tramites_por_estado{estado}}: medidor (gauge) en tiempo real del número de trámites
 *       en cada estado del ciclo de vida en la proyección CQRS {@code tramites.vista_tramite}, utilizado
 *       por el tablero <i>"Flujo de trámite"</i> en Grafana.</li>
 * </ul>
 * <p><b>MODELO DE REFERENCIA PARA EL PROYECTO FINAL:</b>
 * Muestra cómo registrar contadores etiquetados ({@link Counter}) y medidores ({@link Gauge}) en
 * {@link MeterRegistry} para que el agente de OpenTelemetry y el endpoint {@code /actuator/prometheus}
 * los exporten automáticamente hacia Prometheus/Mimir en {@code m4-lgtm}.
 * <p>Concepto: Observabilidad – Métricas de negocio con Micrometer (sección 4.2 del plan).
 * <p>Sesión: S6.
 */
@Component
public class MicrometerMetricasTramiteAdapter implements MetricasTramitePort {

    public static final String METRICA_TRAMITES_REGISTRADOS = "consular_tramites_registrados";
    public static final String METRICA_TRAMITES_POR_ESTADO = "consular_tramites_por_estado";

    private final Map<TipoTramite, Counter> contadoresPorTipo = new EnumMap<>(TipoTramite.class);

    public MicrometerMetricasTramiteAdapter(MeterRegistry meterRegistry,
                                            SpringDataVistaTramiteRepository vistaRepository) {
        for (TipoTramite tipo : TipoTramite.values()) {
            Counter counter = Counter.builder(METRICA_TRAMITES_REGISTRADOS)
                    .description("Total de trámites consulares registrados por tipo")
                    .tag("tipo", tipo.name())
                    .register(meterRegistry);
            contadoresPorTipo.put(tipo, counter);
        }

        for (EstadoTramite estado : EstadoTramite.values()) {
            Gauge.builder(METRICA_TRAMITES_POR_ESTADO, vistaRepository, repo -> {
                        try {
                            return repo.countByEstado(estado.name());
                        } catch (Exception ignored) {
                            return 0.0;
                        }
                    })
                    .description("Cantidad actual de trámites por estado en la proyección CQRS")
                    .tag("estado", estado.name())
                    .register(meterRegistry);
        }
    }

    @Override
    public void incrementarTramitesRegistrados(TipoTramite tipoTramite) {
        Counter counter = contadoresPorTipo.get(tipoTramite);
        if (counter != null) {
            counter.increment();
        }
    }
}

