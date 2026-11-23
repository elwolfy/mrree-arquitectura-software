package pe.utec.consular.pagos.adapter.out.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import pe.utec.consular.pagos.application.port.out.MetricasPagoPort;

/**
 * Adaptador que registra la métrica de negocio {@code consular_pagos_confirmados_total} en Micrometer.
 * <p><b>MODELO DE REFERENCIA PARA EL PROYECTO FINAL:</b>
 * Muestra cómo registrar un contador de eventos de negocio ({@link Counter}) que solo se incrementa
 * cuando un pago se confirma por primera vez (los reintentos idempotentes con la misma {@code Idempotency-Key}
 * no vuelven a incrementar la métrica).
 * <p>Concepto: Observabilidad – Métricas de negocio con Micrometer (sección 4.2 del plan).
 * <p>Sesión: S6.
 */
@Component
public class MicrometerMetricasPagoAdapter implements MetricasPagoPort {

    public static final String METRICA_PAGOS_CONFIRMADOS = "consular_pagos_confirmados";

    private final Counter pagosConfirmadosCounter;

    public MicrometerMetricasPagoAdapter(MeterRegistry meterRegistry) {
        this.pagosConfirmadosCounter = Counter.builder(METRICA_PAGOS_CONFIRMADOS)
                .description("Total de pagos consulares confirmados de forma única")
                .register(meterRegistry);
    }

    @Override
    public void incrementarPagosConfirmados() {
        pagosConfirmadosCounter.increment();
    }
}

