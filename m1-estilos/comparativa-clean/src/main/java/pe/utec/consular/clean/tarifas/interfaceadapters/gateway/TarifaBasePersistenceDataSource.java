package pe.utec.consular.clean.tarifas.interfaceadapters.gateway;

import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Contrato de persistencia de tarifas base que {@code frameworks.persistence} implementa para {@link TarifaBaseJpaGateway}.
 */
public interface TarifaBasePersistenceDataSource {

    Optional<BigDecimal> obtenerMontoBase(TipoTramite tipoTramite);
}

