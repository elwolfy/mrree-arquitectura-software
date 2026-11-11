package pe.utec.consular.clean.tarifas.interfaceadapters.presenter;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.math.BigDecimal;

/**
 * ViewModel JSON de respuesta para una cotización de tarifa consular (Círculo 3: Interface Adapters).
 * <p>Concepto: Clean Architecture – ViewModel (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code TarifaDto} en {@code monolito-modular}.
 */
@Schema(description = "Cotización calculada de tarifa consular")
public record TarifaViewModel(
        @Schema(description = "Tipo de trámite consultado", example = "APOSTILLA")
        TipoTramite tipoTramite,

        @Schema(description = "Código ISO del país de atención", example = "ES")
        String pais,

        @Schema(description = "Código de moneda ISO 4217", example = "PEN")
        String moneda,

        @Schema(description = "Monto total calculado con recargos aplicables", example = "33.00")
        BigDecimal monto,

        @Schema(description = "Nombre simple de la estrategia de tarifación aplicada", example = "ApostillaTarifaStrategy")
        String estrategia
) {
}

