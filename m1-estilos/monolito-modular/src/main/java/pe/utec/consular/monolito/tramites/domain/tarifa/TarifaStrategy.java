package pe.utec.consular.monolito.tramites.domain.tarifa;

import pe.utec.consular.monolito.compartido.domain.TipoTramite;

import java.math.BigDecimal;

/**
 * Interfaz de la estrategia para el cálculo de tarifas consulares.
 * <p>Concepto: Patrón Strategy (sección 2.1 del plan).
 * <p>Sesión: S2. Permite incorporar nuevos algoritmos de tarifación (ej. REGISTRO_CONSULAR)
 * sin modificar la lógica existente, cumpliendo el principio Abierto/Cerrado (OCP).
 */
public interface TarifaStrategy {

    /**
     * Determina si esta estrategia soporta el tipo de trámite dado.
     */
    boolean soporta(TipoTramite tipoTramite);

    /**
     * Calcula el monto final de la tarifa para el país de destino.
     *
     * @param pais código de dos letras del país (ej. "ES", "PE", "US")
     * @return monto en PEN con recargos aplicados
     */
    BigDecimal calcular(String pais);
}
