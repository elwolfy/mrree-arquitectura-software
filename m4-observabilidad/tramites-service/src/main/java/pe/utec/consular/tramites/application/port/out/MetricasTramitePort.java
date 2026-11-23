package pe.utec.consular.tramites.application.port.out;

import pe.utec.consular.tramites.domain.TipoTramite;

/**
 * Puerto de salida para registrar métricas de negocio del contexto de trámites.
 * <p><b>MODELO DE REFERENCIA PARA EL PROYECTO FINAL:</b>
 * Ilustra cómo desacoplar el caso de uso de la biblioteca de telemetría (Micrometer)
 * mediante un puerto secundario en arquitectura hexagonal, registrando el contador
 * {@code consular_tramites_registrados_total{tipo}} (sección 4.2 del plan y sección 7 del contrato).
 * <p>Concepto: Observabilidad – Métricas de negocio con Micrometer / OpenTelemetry.
 * <p>Sesión: S6.
 */
public interface MetricasTramitePort {

    void incrementarTramitesRegistrados(TipoTramite tipoTramite);
}

