package pe.utec.consular.clean.citas.usecase.cancelar;

import java.util.UUID;

/**
 * Frontera de entrada (Input Boundary) para cancelar una cita activa.
 * <p>Concepto: Clean Architecture – Input Boundary (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code CancelarCitaUseCase} en {@code monolito-modular}.
 */
public interface CancelarCitaInputBoundary {

    void cancelar(UUID citaId);
}

