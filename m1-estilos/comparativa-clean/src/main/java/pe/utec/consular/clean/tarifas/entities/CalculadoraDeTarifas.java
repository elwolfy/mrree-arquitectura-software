package pe.utec.consular.clean.tarifas.entities;

import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Calculadora de tarifas consulares que aplica el patrón Strategy en el círculo {@code entities}.
 * <p>Concepto: Patrón Strategy y Principio Abierto/Cerrado (sección 2.1 del plan).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.tramites.domain.tarifa.CalculadoraDeTarifas}.
 */
public class CalculadoraDeTarifas {

    private final List<TarifaStrategy> estrategias;

    public record ResultadoTarifa(
            TipoTramite tipoTramite,
            String pais,
            String moneda,
            BigDecimal monto,
            String estrategia
    ) {
    }

    public CalculadoraDeTarifas(List<TarifaStrategy> estrategias) {
        this.estrategias = Objects.requireNonNull(estrategias, "La lista de estrategias no puede ser nula");
    }

    public ResultadoTarifa calcular(TipoTramite tipoTramite, String pais, Optional<BigDecimal> montoBaseConfigurado) {
        TarifaStrategy estrategiaSeleccionada = estrategias.stream()
                .filter(estrategia -> estrategia.soporta(tipoTramite))
                .findFirst()
                .orElseThrow(() -> new TarifaNoDisponibleException(
                        "No existe estrategia tarifaria configurada para el tipo de trámite: " + tipoTramite));

        BigDecimal montoCalculado = estrategiaSeleccionada.calcular(pais, montoBaseConfigurado);
        return new ResultadoTarifa(
                tipoTramite,
                pais != null ? pais.toUpperCase() : "PE",
                "PEN",
                montoCalculado,
                estrategiaSeleccionada.getClass().getSimpleName()
        );
    }
}

