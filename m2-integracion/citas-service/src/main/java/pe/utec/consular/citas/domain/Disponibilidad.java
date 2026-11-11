package pe.utec.consular.citas.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Modelo de disponibilidad de cupos para una oficina consular en una fecha dada.
 * <p>Concepto: Modelo de dominio de consulta en arquitectura hexagonal (sección 1.3).
 * <p>Sesión: S3.
 */
public record Disponibilidad(
        String oficinaId,
        LocalDate fecha,
        List<CupoDisponibilidad> cupos,
        String origen,
        long actualizadoHaceSegundos
) {
    public record CupoDisponibilidad(
            LocalTime hora,
            int capacidad,
            int ocupados,
            int libres
    ) {
    }
}

