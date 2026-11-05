package pe.utec.consular.monolito.tramites.domain.tarifa;

import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;
import pe.utec.consular.monolito.tramites.application.port.out.TarifaBaseRepositoryPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Estrategia de cálculo de tarifas para trámites de Apostilla.
 * Tarifa base: 30.00 PEN. Recargo para países fuera de Sudamérica: +10%.
 * <p>Concepto: Patrón Strategy (sección 2.1 del plan).
 * <p>Sesión: S2.
 */
@Component
public class ApostillaTarifaStrategy implements TarifaStrategy {

    private static final BigDecimal TARIFA_BASE_DEFECTO = new BigDecimal("30.00");
    private static final BigDecimal FACTOR_RECARGO = new BigDecimal("1.10");
    private static final Set<String> PAISES_SUDAMERICA = Set.of(
            "PE", "AR", "BO", "BR", "CL", "CO", "EC", "GY", "PY", "SR", "UY", "VE"
    );

    private final TarifaBaseRepositoryPort tarifaBaseRepositoryPort;

    public ApostillaTarifaStrategy(TarifaBaseRepositoryPort tarifaBaseRepositoryPort) {
        this.tarifaBaseRepositoryPort = tarifaBaseRepositoryPort;
    }

    @Override
    public boolean soporta(TipoTramite tipoTramite) {
        return TipoTramite.APOSTILLA.equals(tipoTramite);
    }

    @Override
    public BigDecimal calcular(String pais) {
        BigDecimal base = tarifaBaseRepositoryPort.obtenerMontoBase(TipoTramite.APOSTILLA)
                .orElse(TARIFA_BASE_DEFECTO);

        if (pais != null && !PAISES_SUDAMERICA.contains(pais.trim().toUpperCase())) {
            return base.multiply(FACTOR_RECARGO).setScale(2, RoundingMode.HALF_UP);
        }
        return base.setScale(2, RoundingMode.HALF_UP);
    }
}
