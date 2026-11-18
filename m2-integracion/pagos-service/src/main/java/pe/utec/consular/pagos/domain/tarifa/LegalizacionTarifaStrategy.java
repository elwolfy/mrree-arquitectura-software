package pe.utec.consular.pagos.domain.tarifa;

import org.springframework.stereotype.Component;
import pe.utec.consular.pagos.domain.TipoTramite;

import java.math.BigDecimal;

/**
 * Estrategia de cálculo arancelario para {@link TipoTramite#LEGALIZACION} (base 20.00 PEN).
 */
@Component
public class LegalizacionTarifaStrategy implements TarifaStrategy {

    private static final BigDecimal MONTO_BASE = new BigDecimal("20.00");

    @Override
    public TipoTramite tipoTramiteSoportado() {
        return TipoTramite.LEGALIZACION;
    }

    @Override
    public BigDecimal calcularMonto(String pais) {
        return aplicarRecargoExterior(MONTO_BASE, pais);
    }
}

