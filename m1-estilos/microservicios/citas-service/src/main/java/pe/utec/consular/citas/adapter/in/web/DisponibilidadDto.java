package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de disponibilidad de cupos de una oficina consular en una fecha dada.
 */
@Schema(description = "Disponibilidad diaria de cupos")
public record DisponibilidadDto(
        @Schema(example = "MAD") String oficinaId,
        @Schema(example = "2026-11-30") LocalDate fecha,
        List<CupoDisponibilidadDto> cupos,
        @Schema(example = "BASE_DE_DATOS") String origen,
        @Schema(example = "0") long actualizadoHaceSegundos
) {
}

