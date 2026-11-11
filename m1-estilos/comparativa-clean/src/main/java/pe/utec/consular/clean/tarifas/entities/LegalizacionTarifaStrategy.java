package pe.utec.consular.clean.tarifas.entities;

import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;

/**
 * Estrategia de cálculo de tarifas para trámites de Legalización (base 20.00 PEN, +10% fuera de Sudamérica).
 * <p>Concepto: Patrón Strategy en Clean Architecture ({@code entities}, sin anotaciones de Spring).
 * <p>Sesión: S2. Compárese con {@code LegalizacionTarifaStrategy} en {@code monolito-modular}.
 */
public class LegalizacionTarifaStrategy implements TarifaStrategy {

    private static final BigDecimal TARIFA_BASE_DEFECTO = new BigDecimal("20.00");
    private static final BigDecimal FACTOR_RECARGO = new BigDecimal("1.10");
    private static final Set<String> PAISES_SUDAMERICA = Set.of(
            "PE", "AR", "BO", "BR", "CL", "CO", "EC", "GY", "PY", "SR", "UY", "VE"
    );

    @Override
    public boolean soporta(TipoTramite tipoTramite) {
        return TipoTramite.LEGALIZACION.equals(tipoTramite);
    }

    @Override
    public BigDecimal calcular(String pais, Optional<BigDecimal> montoBaseConfigurado) {
        BigDecimal base = montoBaseConfigurado.orElse(TARIFA_BASE_DEFECTO);
        if (pais != null && !PAISES_SUDAMERICA.contains(pais.trim().toUpperCase())) {
            return base.multiply(FACTOR_RECARGO).setScale(2, RoundingMode.HALF_UP);
        }
        return base.setScale(2, RoundingMode.HALF_UP);
    }
}

