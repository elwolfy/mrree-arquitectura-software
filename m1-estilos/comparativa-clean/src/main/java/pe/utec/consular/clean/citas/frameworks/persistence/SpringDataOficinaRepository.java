package pe.utec.consular.clean.citas.frameworks.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para oficinas consulares ({@code frameworks.persistence}).
 */
@Repository
public interface SpringDataOficinaRepository extends JpaRepository<OficinaJpaEntity, String> {
}

