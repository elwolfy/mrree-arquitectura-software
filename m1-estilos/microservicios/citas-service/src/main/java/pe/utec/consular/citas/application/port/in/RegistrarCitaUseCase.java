package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.Cita;

/**
 * Puerto de entrada para registrar una cita consular.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3 del plan).
 * <p>Sesión: S1 y S2.
 */
public interface RegistrarCitaUseCase {

    Cita registrar(RegistrarCitaCommand comando);
}

