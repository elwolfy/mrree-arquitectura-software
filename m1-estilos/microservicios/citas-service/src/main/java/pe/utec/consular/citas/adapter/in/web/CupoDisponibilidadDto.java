package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

/**
 * DTO de una franja horaria dentro de la disponibilidad diaria.
 */
@Schema(description = "Franja horaria con capacidad disponible")
public record CupoDisponibilidadDto(
        @Schema(example = "08:00") LocalTime hora,
        @Schema(example = "2") int capacidad,
        @Schema(example = "1") int ocupados,
        @Schema(example = "1") int libres
) {
}

