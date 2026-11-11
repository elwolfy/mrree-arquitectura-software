package pe.utec.consular.clean.citas.usecase.oficinas;

import java.util.List;

/**
 * Frontera de salida (Output Boundary) para presentar la lista de oficinas consulares.
 * <p>Concepto: Clean Architecture – Output Boundary (sección 1.3 del plan).
 */
public interface ListarOficinasOutputBoundary {

    void presentar(List<ListarOficinasResponseModel> oficinas);
}

