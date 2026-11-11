package pe.utec.consular.clean.citas.usecase.disponibilidad;

import pe.utec.consular.clean.citas.entities.Disponibilidad.CupoDisponibilidad;

import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de respuesta (Response Model) de disponibilidad de cupos.
 * <p>Concepto: Clean Architecture – Response Model (sección 1.3 del plan).
 */
public record ConsultarDisponibilidadResponseModel(
        String oficinaId,
        LocalDate fecha,
        List<CupoDisponibilidad> cupos,
        String origen,
        long actualizadoHaceSegundos
) {
}

