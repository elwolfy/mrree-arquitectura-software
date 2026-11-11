package pe.utec.consular.clean.citas.usecase.cancelar;

/**
 * Frontera de salida (Output Boundary) tras cancelar una cita.
 * <p>Concepto: Clean Architecture – Output Boundary (sección 1.3 del plan).
 */
public interface CancelarCitaOutputBoundary {

    void presentarCancelacion(CancelarCitaResponseModel responseModel);
}

