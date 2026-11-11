package pe.utec.consular.clean.citas.interfaceadapters.presenter;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ViewModel de presentación de una oficina consular (Círculo 3: Interface Adapters).
 * <p>Concepto: Clean Architecture – ViewModel (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code OficinaDto} en {@code monolito-modular}.
 */
@Schema(description = "Información de una oficina consular en el exterior")
public record OficinaViewModel(
        @Schema(description = "Código identificador de la oficina", example = "MAD")
        String id,

        @Schema(description = "Nombre oficial de la sede consular", example = "Consulado General del Perú en Madrid (laboratorio)")
        String nombre,

        @Schema(description = "Código ISO de 2 letras del país", example = "ES")
        String pais,

        @Schema(description = "Ciudad sede", example = "Madrid")
        String ciudad,

        @Schema(description = "Zona horaria IANA", example = "Europe/Madrid")
        String zonaHoraria
) {
}

