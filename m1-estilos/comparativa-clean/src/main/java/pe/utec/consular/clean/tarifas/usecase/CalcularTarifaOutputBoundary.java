package pe.utec.consular.clean.tarifas.usecase;

/**
 * Frontera de salida (Output Boundary) para presentar la tarifa calculada.
 */
public interface CalcularTarifaOutputBoundary {

    void presentar(CalcularTarifaResponseModel responseModel);
}

