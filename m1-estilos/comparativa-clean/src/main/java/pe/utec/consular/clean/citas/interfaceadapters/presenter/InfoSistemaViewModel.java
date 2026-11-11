package pe.utec.consular.clean.citas.interfaceadapters.presenter;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ViewModel de información del sistema y variante arquitectónica en ejecución.
 * <p>Concepto: Introspección arquitectónica (sección 4.1 del contrato global).
 * En {@code comparativa-clean}, {@code arquitecturaInterna} devuelve {@code CLEAN} y {@code variante} devuelve {@code MONOLITO_MODULAR}.
 */
@Schema(description = "Información de la variante arquitectónica y modo de consistencia en ejecución")
public record InfoSistemaViewModel(
        @Schema(description = "Variante de estilo arquitectónico", example = "MONOLITO_MODULAR")
        String variante,

        @Schema(description = "Estilo de arquitectura interna", example = "CLEAN")
        String arquitecturaInterna,

        @Schema(description = "Modo de consistencia CAP activo", example = "CP")
        String modoConsistencia,

        @Schema(description = "Versión del componente", example = "1.0.0")
        String version
) {
}

