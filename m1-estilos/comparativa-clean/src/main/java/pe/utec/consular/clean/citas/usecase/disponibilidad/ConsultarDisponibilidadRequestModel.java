package pe.utec.consular.clean.citas.usecase.disponibilidad;

import java.time.LocalDate;

/**
 * Modelo de petición (Request Model) para consultar disponibilidad de cupos.
 * <p>Concepto: Clean Architecture – Request Model (sección 1.3 del plan).
 */
public record ConsultarDisponibilidadRequestModel(
        String oficinaId,
        LocalDate fecha
) {
}

