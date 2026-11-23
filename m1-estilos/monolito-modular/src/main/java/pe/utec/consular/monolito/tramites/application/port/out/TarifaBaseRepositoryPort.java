package pe.utec.consular.monolito.tramites.application.port.out;

import pe.utec.consular.monolito.compartido.domain.TipoTramite;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Puerto de salida para consultar las tarifas base persistidas.
 * <p>Concepto: Arquitectura hexagonal – puerto de salida (sección 1.3).
 */
public interface TarifaBaseRepositoryPort {

    Optional<BigDecimal> obtenerMontoBase(TipoTramite tipoTramite);
}
