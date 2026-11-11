package pe.utec.consular.clean.citas.usecase.registrar;

/**
 * Frontera de salida (Output Boundary) para presentar el resultado de registrar una cita.
 * <p>Concepto: Clean Architecture – Output Boundary e inversión de dependencias hacia el Presentador (sección 1.3 del plan).
 * El {@link RegistrarCitaInteractor} invoca esta interfaz sin conocer cómo se formateará la respuesta
 * (JSON REST, vista web, CLI). En la arquitectura hexagonal del monolito no existe esta interfaz:
 * el caso de uso devuelve {@code Cita} directamente al controlador.
 * <p>Sesión: S2. Implementada por {@code CitaPresenter} en {@code interfaceadapters.presenter}.
 */
public interface RegistrarCitaOutputBoundary {

    void presentar(RegistrarCitaResponseModel responseModel);
}

