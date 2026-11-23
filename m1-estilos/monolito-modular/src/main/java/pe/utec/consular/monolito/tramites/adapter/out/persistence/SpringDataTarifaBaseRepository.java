package pe.utec.consular.monolito.tramites.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data para tramites.tarifa_base.
 */
@Repository
public interface SpringDataTarifaBaseRepository extends JpaRepository<TarifaBaseJpaEntity, String> {
}
