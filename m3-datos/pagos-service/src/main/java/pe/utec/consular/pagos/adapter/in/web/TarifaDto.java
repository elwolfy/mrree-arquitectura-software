package pe.utec.consular.pagos.adapter.in.web;

import pe.utec.consular.pagos.domain.TipoTramite;
import pe.utec.consular.pagos.domain.tarifa.Tarifa;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el cálculo arancelario (sección 4.1 y 6 del contrato global).
 */
public record TarifaDto(
        TipoTramite tipoTramite,
        String pais,
        String moneda,
        BigDecimal monto,
        String estrategia
) {
    public static TarifaDto desdeDominio(Tarifa tarifa) {
        return new TarifaDto(
                tarifa.tipoTramite(),
                tarifa.pais(),
                tarifa.moneda(),
                tarifa.monto(),
                tarifa.estrategia()
        );
    }
}

