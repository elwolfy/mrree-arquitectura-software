package pe.utec.consular.clean.tarifas.usecase;

/**
 * Frontera de entrada (Input Boundary) para calcular tarifas consulares.
 */
public interface CalcularTarifaInputBoundary {

    void calcular(CalcularTarifaRequestModel requestModel);
}

