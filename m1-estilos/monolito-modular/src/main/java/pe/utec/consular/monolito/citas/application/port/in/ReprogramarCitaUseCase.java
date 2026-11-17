package pe.utec.consular.monolito.citas.application.port.in;

import pe.utec.consular.monolito.citas.domain.Cita;

/**
 * Puerto de entrada para el caso de uso de reprogramación de una cita consular.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3).
 * <p>Sesión: S2 (Asignación A1).
 */
public interface ReprogramarCitaUseCase {

    Cita reprogramar(ReprogramarCitaCommand comando);
}

