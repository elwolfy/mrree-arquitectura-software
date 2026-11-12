package pe.utec.consular.citas.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Modelo de dominio para la disponibilidad diaria de una oficina consular.
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

