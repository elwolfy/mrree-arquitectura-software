package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Información sobre la variante arquitectónica y estado de consistencia del servicio en ejecución.
 */
@Schema(description = "Metadatos y estado de la variante arquitectónica activa")
public record InfoSistemaDto(
        @Schema(description = "Variante de arquitectura de estilos", example = "MICROSERVICIOS")
        String variante,

        @Schema(description = "Estilo de arquitectura interna del módulo", example = "HEXAGONAL")
        String arquitecturaInterna,

        @Schema(description = "Modo de consistencia activo (CP o AP)", example = "CP")
        String modoConsistencia,

        @Schema(description = "Versión del artefacto", example = "1.0.0")
        String version
) {
}

