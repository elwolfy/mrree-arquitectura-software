package pe.utec.consular.pagos.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@code pagos.pago}.
 */
@Repository
public interface SpringDataPagoRepository extends JpaRepository<PagoJpaEntity, UUID> {

    Optional<PagoJpaEntity> findByClaveIdempotencia(String claveIdempotencia);

    List<PagoJpaEntity> findByTramiteIdOrderByCreadoEnDesc(UUID tramiteId);
}

