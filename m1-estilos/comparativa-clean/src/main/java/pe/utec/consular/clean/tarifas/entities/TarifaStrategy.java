package pe.utec.consular.clean.tarifas.entities;

import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Interfaz de estrategia para el cálculo de tarifas consulares en el círculo {@code entities}.
 * <p>Concepto: Patrón Strategy y Principio Abierto/Cerrado (OCP, sección 2.1 del plan) en Clean Architecture.
 * Sin anotaciones de Spring; las estrategias se registran en {@code frameworks.config.TarifasBeansConfig}.
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.tramites.domain.tarifa.TarifaStrategy}.
 */
public interface TarifaStrategy {

    boolean soporta(TipoTramite tipoTramite);

    BigDecimal calcular(String pais, Optional<BigDecimal> montoBaseConfigurado);
}

