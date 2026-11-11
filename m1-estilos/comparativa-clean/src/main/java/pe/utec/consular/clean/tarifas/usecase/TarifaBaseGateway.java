package pe.utec.consular.clean.tarifas.usecase;

import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Gateway para consultar la tarifa base configurada en la base de datos ({@code tramites.tarifa_base}).
 * <p>Concepto: Clean Architecture – Entity Gateway en {@code usecase} (sección 1.3 del plan).
 */
public interface TarifaBaseGateway {

    Optional<BigDecimal> obtenerMontoBase(TipoTramite tipoTramite);
}

