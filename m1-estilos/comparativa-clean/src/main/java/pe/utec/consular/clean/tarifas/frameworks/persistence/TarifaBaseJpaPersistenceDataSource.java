package pe.utec.consular.clean.tarifas.frameworks.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.clean.citas.entities.TipoTramite;
import pe.utec.consular.clean.tarifas.interfaceadapters.gateway.TarifaBasePersistenceDataSource;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementación JPA del origen de datos de tarifas base en {@code frameworks.persistence}.
 */
@Component
public class TarifaBaseJpaPersistenceDataSource implements TarifaBasePersistenceDataSource {

    private final SpringDataTarifaBaseRepository repository;

    public TarifaBaseJpaPersistenceDataSource(SpringDataTarifaBaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BigDecimal> obtenerMontoBase(TipoTramite tipoTramite) {
        return repository.findById(tipoTramite.name())
                .map(TarifaBaseJpaEntity::getMonto);
    }
}

