package pe.utec.consular.pagos.application.port.out;

/**
 * Puerto de salida para registrar métricas de negocio del contexto de pagos.
 * <p><b>MODELO DE REFERENCIA PARA EL PROYECTO FINAL:</b>
 * Ilustra cómo registrar el contador {@code consular_pagos_confirmados_total} en Micrometer
 * (sección 4.2 del plan y sección 7 del contrato) sin acoplar la capa de aplicación a la infraestructura.
 * <p>Concepto: Observabilidad – Métricas de negocio con Micrometer / OpenTelemetry.
 * <p>Sesión: S6.
 */
public interface MetricasPagoPort {

    void incrementarPagosConfirmados();
}

