package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO con metadatos arquitectónicos de la variante en ejecución.
 */
@Schema(description = "Información de la variante arquitectónica activa")
public record InfoSistemaDto(
        @Schema(example = "MICROSERVICIOS") String variante,
        @Schema(example = "HEXAGONAL") String arquitecturaInterna,
        @Schema(example = "CP") String modoConsistencia,
        @Schema(example = "1.0.0") String version
) {
}

