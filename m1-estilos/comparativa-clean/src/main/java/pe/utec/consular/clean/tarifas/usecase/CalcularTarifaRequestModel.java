package pe.utec.consular.clean.tarifas.usecase;

import pe.utec.consular.clean.citas.entities.TipoTramite;

/**
 * Modelo de petición (Request Model) para el cálculo de tarifa consular.
 */
public record CalcularTarifaRequestModel(
        TipoTramite tipoTramite,
        String pais
) {
}

