package pe.utec.consular.monolito.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

/**
 * Franja horaria con capacidad y cupos libres para disponibilidad.
 */
@Schema(description = "Detalle de cupo y disponibilidad por hora")
public record CupoDisponibilidadDto(
        @Schema(description = "Hora del cupo", example = "08:00")
        LocalTime hora,

        @Schema(description = "Capacidad máxima de atención", example = "2")
        int capacidad,

        @Schema(description = "Cantidad de cupos actualmente ocupados", example = "1")
        int ocupados,

        @Schema(description = "Cantidad de cupos disponibles", example = "1")
        int libres
) {
}
