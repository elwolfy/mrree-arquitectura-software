package pe.utec.consular.clean.citas.usecase.disponibilidad;

/**
 * Frontera de entrada (Input Boundary) para consultar disponibilidad de cupos.
 * <p>Concepto: Clean Architecture – Input Boundary (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code ConsultarDisponibilidadUseCase} en {@code monolito-modular}.
 */
public interface ConsultarDisponibilidadInputBoundary {

    void consultar(ConsultarDisponibilidadRequestModel requestModel);
}

