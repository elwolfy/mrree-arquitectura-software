package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representación HTTP de una oficina consular según contrato global (sección 4.1).
 */
@Schema(description = "Datos de una oficina consular del Perú")
public record OficinaDto(
        @Schema(description = "Identificador de la oficina", example = "MAD")
        String id,

        @Schema(description = "Nombre oficial de la sede", example = "Consulado General del Perú en Madrid (laboratorio)")
        String nombre,

        @Schema(description = "Código de país de dos letras", example = "ES")
        String pais,

        @Schema(description = "Ciudad donde se ubica", example = "Madrid")
        String ciudad,

        @Schema(description = "Zona horaria IANA", example = "Europe/Madrid")
        String zonaHoraria
) {
}

