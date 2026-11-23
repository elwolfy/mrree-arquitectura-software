package pe.utec.consular.pagos.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.pagos.application.port.out.PagoRepositoryPort;
import pe.utec.consular.pagos.domain.EstadoPago;
import pe.utec.consular.pagos.domain.OrdenDePago;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para {@link PagoRepositoryPort}.
 */
@Component
public class PagoJpaAdapter implements PagoRepositoryPort {

    private final SpringDataPagoRepository repository;

    public PagoJpaAdapter(SpringDataPagoRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrdenDePago guardar(OrdenDePago ordenDePago) {
        PagoJpaEntity entity = new PagoJpaEntity(
                ordenDePago.getId(),
                ordenDePago.getTramiteId(),
                ordenDePago.getMonto(),
                ordenDePago.getEstado().name(),
                ordenDePago.getClaveIdempotencia(),
                ordenDePago.getCreadaEn()
        );
        PagoJpaEntity guardada = repository.saveAndFlush(entity);
        return aDominio(guardada);
    }

    @Override
    public Optional<OrdenDePago> buscarPorId(UUID pagoId) {
        return repository.findById(pagoId).map(this::aDominio);
    }

    @Override
    public Optional<OrdenDePago> buscarPorClaveIdempotencia(String claveIdempotencia) {
        return repository.findByClaveIdempotencia(claveIdempotencia).map(this::aDominio);
    }

    @Override
    public List<OrdenDePago> buscarPorTramiteId(UUID tramiteId) {
        return repository.findByTramiteIdOrderByCreadoEnDesc(tramiteId).stream()
                .map(this::aDominio)
                .toList();
    }

    private OrdenDePago aDominio(PagoJpaEntity entity) {
        return new OrdenDePago(
                entity.getId(),
                entity.getTramiteId(),
                entity.getMonto(),
                "PEN",
                EstadoPago.valueOf(entity.getEstado()),
                entity.getClaveIdempotencia(),
                entity.getCreadoEn()
        );
    }
}

