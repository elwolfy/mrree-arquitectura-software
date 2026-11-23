package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la tabla {@code tramites.outbox}.
 */
@Repository
public interface SpringDataOutboxRepository extends JpaRepository<OutboxJpaEntity, UUID> {

    List<OutboxJpaEntity> findByPublicadoEnIsNullOrderByCreadoEnAsc();
}

