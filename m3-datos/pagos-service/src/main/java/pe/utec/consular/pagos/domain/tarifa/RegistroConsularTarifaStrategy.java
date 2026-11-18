package pe.utec.consular.pagos.domain.tarifa;

import org.springframework.stereotype.Component;
import pe.utec.consular.pagos.domain.TipoTramite;

import java.math.BigDecimal;

/**
 * Estrategia de cálculo arancelario para {@link TipoTramite#REGISTRO_CONSULAR} (base 25.00 PEN).
 * <p>En M1 se agrega en vivo durante la sesión S2 para demostrar el principio Abierto/Cerrado;
 * en M2 ya forma parte integral de la plataforma.
 */
@Component
public class RegistroConsularTarifaStrategy implements TarifaStrategy {

    private static final BigDecimal MONTO_BASE = new BigDecimal("25.00");

    @Override
    public TipoTramite tipoTramiteSoportado() {
        return TipoTramite.REGISTRO_CONSULAR;
    }

    @Override
    public BigDecimal calcularMonto(String pais) {
        return aplicarRecargoExterior(MONTO_BASE, pais);
    }
}

