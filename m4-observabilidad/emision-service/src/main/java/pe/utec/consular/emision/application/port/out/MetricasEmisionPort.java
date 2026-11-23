package pe.utec.consular.emision.application.port.out;

/**
 * Puerto de salida para registrar la duración de la emisión documental en un histograma de Micrometer.
 * <p><b>MODELO DE REFERENCIA PARA EL PROYECTO FINAL:</b>
 * Muestra cómo registrar la métrica {@code consular_emision_duracion_seconds} como un {@code Timer}
 * con histograma de percentiles (p50, p95, p99) exportado a Prometheus/Mimir.
 * <p>Concepto: Observabilidad – Histogramas de latencia de negocio con Micrometer (sección 4.2 del plan).
 * <p>Sesión: S6.
 */
public interface MetricasEmisionPort {

    void registrarDuracionEmisionNanos(long duracionNanos);
}

