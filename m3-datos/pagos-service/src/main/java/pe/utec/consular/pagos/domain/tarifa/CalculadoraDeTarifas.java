package pe.utec.consular.pagos.domain.tarifa;

import org.springframework.stereotype.Service;
import pe.utec.consular.pagos.domain.TipoTramite;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Contexto del patrón Strategy que resuelve la tarifa aplicable inyectando
 * todas las implementaciones de {@link TarifaStrategy} como {@code List<TarifaStrategy>}
 * sin utilizar condicionales por tipo.
 */
@Service
public class CalculadoraDeTarifas {

    private final Map<TipoTramite, TarifaStrategy> estrategiasPorTipo;

    public CalculadoraDeTarifas(List<TarifaStrategy> estrategias) {
        this.estrategiasPorTipo = new EnumMap<>(TipoTramite.class);
        for (TarifaStrategy estrategia : estrategias) {
            this.estrategiasPorTipo.put(estrategia.tipoTramiteSoportado(), estrategia);
        }
    }

    public Tarifa calcular(TipoTramite tipoTramite, String pais) {
        if (tipoTramite == null) {
            throw new IllegalArgumentException("El tipo de trámite es obligatorio");
        }
        if (pais == null || !pais.trim().matches("^[A-Za-z]{2}$")) {
            throw new IllegalArgumentException("El código de país debe constar de 2 letras ISO");
        }

        TarifaStrategy estrategia = estrategiasPorTipo.get(tipoTramite);
        if (estrategia == null) {
            throw new IllegalStateException("No existe estrategia de tarifa registrada para " + tipoTramite);
        }

        String paisNormalizado = pais.trim().toUpperCase();
        BigDecimal monto = estrategia.calcularMonto(paisNormalizado);
        return new Tarifa(
                tipoTramite,
                paisNormalizado,
                "PEN",
                monto,
                estrategia.getClass().getSimpleName()
        );
    }
}

