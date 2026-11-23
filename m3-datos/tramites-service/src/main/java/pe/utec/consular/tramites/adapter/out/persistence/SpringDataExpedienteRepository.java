package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para el documento {@code tramites.expediente}.
 */
@Repository
public interface SpringDataExpedienteRepository extends JpaRepository<ExpedienteJpaEntity, UUID> {
}

