package pe.utec.consular.monolito.citas.application.port.in;

import pe.utec.consular.monolito.citas.domain.Cita;

/**
 * Puerto de entrada para registrar una cita consular.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code RegistrarCitaInputBoundary} en comparativa-clean.
 */
public interface RegistrarCitaUseCase {

    Cita registrar(RegistrarCitaCommand comando);
}
