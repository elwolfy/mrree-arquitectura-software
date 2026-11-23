package pe.utec.consular.citas.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para oficinas consulares.
 */
@Repository
public interface SpringDataOficinaRepository extends JpaRepository<OficinaJpaEntity, String> {
}

