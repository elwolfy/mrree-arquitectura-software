package pe.utec.consular.clean.tarifas.usecase;

import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.math.BigDecimal;

/**
 * Modelo de respuesta (Response Model) del cálculo de tarifa consular.
 */
public record CalcularTarifaResponseModel(
        TipoTramite tipoTramite,
        String pais,
        String moneda,
        BigDecimal monto,
        String estrategia
) {
}

