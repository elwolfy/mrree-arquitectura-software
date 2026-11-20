package pe.utec.consular.pagos.domain.tarifa;

import pe.utec.consular.pagos.domain.TipoTramite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Contrato del patrón de comportamiento Strategy (sección 2.1) para el cálculo arancelario.
 */
public interface TarifaStrategy {

    Set<String> PAISES_SUDAMERICA = Set.of("PE", "AR", "BO", "BR", "CL", "CO", "EC", "GY", "PY", "SR", "UY", "VE");
    BigDecimal RECARGO_EXTERIOR = new BigDecimal("1.10");

    TipoTramite tipoTramiteSoportado();

    BigDecimal calcularMonto(String pais);

    default BigDecimal aplicarRecargoExterior(BigDecimal montoBase, String pais) {
        if (pais != null && PAISES_SUDAMERICA.contains(pais.trim().toUpperCase())) {
            return montoBase.setScale(2, RoundingMode.HALF_UP);
        }
        return montoBase.multiply(RECARGO_EXTERIOR).setScale(2, RoundingMode.HALF_UP);
    }
}

