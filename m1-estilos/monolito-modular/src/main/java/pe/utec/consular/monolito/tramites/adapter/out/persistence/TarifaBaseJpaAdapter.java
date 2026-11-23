package pe.utec.consular.monolito.tramites.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;
import pe.utec.consular.monolito.tramites.application.port.out.TarifaBaseRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Adaptador JPA para el puerto de salida de tarifas base.
 */
@Component
public class TarifaBaseJpaAdapter implements TarifaBaseRepositoryPort {

    private final SpringDataTarifaBaseRepository repository;

    public TarifaBaseJpaAdapter(SpringDataTarifaBaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BigDecimal> obtenerMontoBase(TipoTramite tipoTramite) {
        return repository.findById(tipoTramite.name())
                .map(TarifaBaseJpaEntity::getMonto);
    }
}
