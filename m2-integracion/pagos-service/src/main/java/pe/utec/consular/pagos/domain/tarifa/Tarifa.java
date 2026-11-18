package pe.utec.consular.pagos.domain.tarifa;

import pe.utec.consular.pagos.domain.TipoTramite;

import java.math.BigDecimal;

/**
 * Resultado del cálculo de tarifa consular.
 */
public record Tarifa(
        TipoTramite tipoTramite,
        String pais,
        String moneda,
        BigDecimal monto,
        String estrategia
) {
}

