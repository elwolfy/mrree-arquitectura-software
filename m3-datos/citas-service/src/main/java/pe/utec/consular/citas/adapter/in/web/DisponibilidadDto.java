package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * Representación HTTP de la disponibilidad de cupos según contrato global (sección 4.1).
 */
@Schema(description = "Disponibilidad de cupos en una oficina consular y fecha")
public record DisponibilidadDto(
        @Schema(description = "Identificador de la oficina", example = "MAD")
        String oficinaId,

        @Schema(description = "Fecha consultada (ISO-8601)", example = "2026-11-30")
        LocalDate fecha,

        @Schema(description = "Lista de cupos con capacidad del día")
        List<CupoDisponibilidadDto> cupos,

        @Schema(description = "Origen de los datos: BASE_DE_DATOS o CACHE", example = "BASE_DE_DATOS")
        String origen,

        @Schema(description = "Antigüedad en segundos si proviene de caché", example = "0")
        long actualizadoHaceSegundos
) {
}

