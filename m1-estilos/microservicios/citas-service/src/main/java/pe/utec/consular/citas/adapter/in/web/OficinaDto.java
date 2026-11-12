package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para una oficina consular.
 */
@Schema(description = "Oficina consular")
public record OficinaDto(
        @Schema(example = "MAD") String id,
        @Schema(example = "Consulado General del Perú en Madrid (laboratorio)") String nombre,
        @Schema(example = "ES") String pais,
        @Schema(example = "Madrid") String ciudad,
        @Schema(example = "Europe/Madrid") String zonaHoraria
) {
}

