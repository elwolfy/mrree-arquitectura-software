package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@code tramites.saga_instancia}.
 */
@Repository
public interface SpringDataSagaInstanciaRepository extends JpaRepository<SagaInstanciaJpaEntity, UUID> {
}

