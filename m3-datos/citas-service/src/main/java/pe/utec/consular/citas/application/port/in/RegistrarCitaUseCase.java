package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.Cita;

/**
 * Puerto de entrada para registrar una nueva cita consular.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3).
 * <p>Sesión: S3.
 */
public interface RegistrarCitaUseCase {

    Cita registrar(RegistrarCitaCommand comando);
}

