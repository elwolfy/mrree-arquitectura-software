package pe.utec.consular.clean.citas.usecase.registrar;

/**
 * Frontera de entrada (Input Boundary) para registrar una cita consular.
 * <p>Concepto: Clean Architecture – Input Boundary (sección 1.3 del plan).
 * A diferencia de {@code RegistrarCitaUseCase} en la arquitectura hexagonal (que devuelve {@code Cita}),
 * en Clean Architecture el método {@code ejecutar} no devuelve el modelo directamente al controlador,
 * sino que el {@code Interactor} empuja el resultado hacia el {@link RegistrarCitaOutputBoundary}.
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.application.port.in.RegistrarCitaUseCase}.
 */
public interface RegistrarCitaInputBoundary {

    void ejecutar(RegistrarCitaRequestModel requestModel);
}

