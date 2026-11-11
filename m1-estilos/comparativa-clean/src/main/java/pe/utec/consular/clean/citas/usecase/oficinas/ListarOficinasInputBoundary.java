package pe.utec.consular.clean.citas.usecase.oficinas;

/**
 * Frontera de entrada (Input Boundary) para listar las oficinas consulares activas.
 * <p>Concepto: Clean Architecture – Input Boundary (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code ListarOficinasUseCase} en {@code monolito-modular}.
 */
public interface ListarOficinasInputBoundary {

    void listar();
}

