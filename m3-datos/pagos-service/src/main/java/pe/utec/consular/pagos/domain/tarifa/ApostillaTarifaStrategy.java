package pe.utec.consular.pagos.domain.tarifa;

import org.springframework.stereotype.Component;
import pe.utec.consular.pagos.domain.TipoTramite;

import java.math.BigDecimal;

/**
 * Estrategia de cálculo arancelario para {@link TipoTramite#APOSTILLA} (base 30.00 PEN).
 */
@Component
public class ApostillaTarifaStrategy implements TarifaStrategy {

    private static final BigDecimal MONTO_BASE = new BigDecimal("30.00");

    @Override
    public TipoTramite tipoTramiteSoportado() {
        return TipoTramite.APOSTILLA;
    }

    @Override
    public BigDecimal calcularMonto(String pais) {
        return aplicarRecargoExterior(MONTO_BASE, pais);
    }
}

