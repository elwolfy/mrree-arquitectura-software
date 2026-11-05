package pe.utec.consular.monolito.tramites.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;

import java.math.BigDecimal;

/**
 * DTO que representa el resultado del cálculo de una tarifa consular.
 * <p>Concepto: DTO (Data Transfer Object) inmutable según contrato global (sección 4.1).
 */
@Schema(description = "Detalle de cotización de tarifa consular calculada mediante patrón Strategy")
public record TarifaDto(
        @Schema(description = "Tipo de trámite consular", example = "APOSTILLA")
        TipoTramite tipoTramite,

        @Schema(description = "Código de país de atención", example = "ES")
        String pais,

        @Schema(description = "Moneda del monto", example = "PEN")
        String moneda,

        @Schema(description = "Monto final calculado con recargos", example = "33.00")
        BigDecimal monto,

        @Schema(description = "Nombre de la estrategia que realizó el cálculo", example = "ApostillaTarifaStrategy")
        String estrategia
) {
}
