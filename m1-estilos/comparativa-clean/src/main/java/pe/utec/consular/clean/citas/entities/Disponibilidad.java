package pe.utec.consular.clean.citas.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Modelo de disponibilidad de cupos para una oficina consular en una fecha dada.
 * <p>Concepto: Teorema CAP (sección 1.4 del plan) en Clean Architecture ({@code entities}).
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

