package pe.utec.consular.clean.citas.usecase.registrar;

import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Estructura de datos de entrada (Request Model) del caso de uso Registrar Cita.
 * <p>Concepto: Clean Architecture – Request Model en {@code usecase} (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code RegistrarCitaCommand} en {@code monolito-modular}.
 */
public record RegistrarCitaRequestModel(
        String dni,
        String nombres,
        String email,
        String oficinaId,
        LocalDate fecha,
        LocalTime hora,
        TipoTramite tipoTramite
) {
}

