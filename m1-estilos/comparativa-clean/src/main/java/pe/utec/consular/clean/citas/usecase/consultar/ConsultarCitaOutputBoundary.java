package pe.utec.consular.clean.citas.usecase.consultar;

import java.util.List;

/**
 * Frontera de salida (Output Boundary) para presentar citas consultadas.
 * <p>Concepto: Clean Architecture – Output Boundary (sección 1.3 del plan).
 * <p>Sesión: S2.
 */
public interface ConsultarCitaOutputBoundary {

    void presentarCita(ConsultarCitaResponseModel responseModel);

    void presentarListaCitas(List<ConsultarCitaResponseModel> responseModels);
}

