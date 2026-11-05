package pe.utec.consular.monolito.tramites.domain.tarifa;

import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;
import pe.utec.consular.monolito.compartido.domain.exception.TarifaNoDisponibleException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Calculadora de tarifas que selecciona dinámicamente la estrategia adecuada.
 * <p>Concepto: Patrón Strategy y Principio Abierto/Cerrado (sección 2.1 del plan).
 * <p>Sesión: S2. La lista de estrategias se inyecta por Spring; agregar una nueva estrategia
 * (ej. {@code RegistroConsularTarifaStrategy}) no requiere modificar esta clase.
 */
@Component
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

    /**
     * Calcula la tarifa resolviendo la estrategia según el tipo de trámite.
     *
     * @param tipoTramite tipo de trámite a cotizar
     * @param pais código de país
     * @return desglose del resultado tarifario
     * @throws TarifaNoDisponibleException si ninguna estrategia soporta el trámite
     */
    public ResultadoTarifa calcular(TipoTramite tipoTramite, String pais) {
        TarifaStrategy estrategiaSeleccionada = estrategias.stream()
                .filter(estrategia -> estrategia.soporta(tipoTramite))
                .findFirst()
                .orElseThrow(() -> new TarifaNoDisponibleException(
                        "No existe estrategia tarifaria configurada para el tipo de trámite: " + tipoTramite));

        BigDecimal montoCalculado = estrategiaSeleccionada.calcular(pais);
        return new ResultadoTarifa(
                tipoTramite,
                pais != null ? pais.toUpperCase() : "PE",
                "PEN",
                montoCalculado,
                estrategiaSeleccionada.getClass().getSimpleName()
        );
    }
}
