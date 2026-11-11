package pe.utec.consular.clean.tarifas.interfaceadapters.gateway;

import pe.utec.consular.clean.citas.entities.TipoTramite;
import pe.utec.consular.clean.tarifas.usecase.TarifaBaseGateway;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementación del gateway de tarifas base en {@code interfaceadapters.gateway}.
 * <p>Concepto: Clean Architecture – Interface Adapter Gateway (Círculo 3, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code TarifaBaseJpaAdapter} en {@code monolito-modular}.
 */
public class TarifaBaseJpaGateway implements TarifaBaseGateway {

    private final TarifaBasePersistenceDataSource dataSource;

    public TarifaBaseJpaGateway(TarifaBasePersistenceDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<BigDecimal> obtenerMontoBase(TipoTramite tipoTramite) {
        return dataSource.obtenerMontoBase(tipoTramite);
    }
}

