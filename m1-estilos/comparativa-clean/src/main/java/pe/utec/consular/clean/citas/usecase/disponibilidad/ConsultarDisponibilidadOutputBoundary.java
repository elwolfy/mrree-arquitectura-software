package pe.utec.consular.clean.citas.usecase.disponibilidad;

/**
 * Frontera de salida (Output Boundary) para presentar la disponibilidad de cupos.
 * <p>Concepto: Clean Architecture – Output Boundary (sección 1.3 del plan).
 */
public interface ConsultarDisponibilidadOutputBoundary {

    void presentar(ConsultarDisponibilidadResponseModel responseModel);
}

