package pe.utec.consular.clean.citas.usecase.consultar;

import java.util.UUID;

/**
 * Frontera de entrada (Input Boundary) para consultar citas por ID o por DNI.
 * <p>Concepto: Clean Architecture – Input Boundary (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code ConsultarCitaUseCase} en {@code monolito-modular}.
 */
public interface ConsultarCitaInputBoundary {

    void consultarPorId(UUID citaId);

    void consultarPorDni(String dni);
}

