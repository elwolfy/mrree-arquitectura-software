package pe.utec.consular.clean.tarifas.frameworks.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para tarifas base ({@code frameworks.persistence}).
 */
@Repository
public interface SpringDataTarifaBaseRepository extends JpaRepository<TarifaBaseJpaEntity, String> {
}

