package pe.utec.consular.monolito.notificaciones.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para avisos.
 */
@Repository
public interface SpringDataAvisoRepository extends JpaRepository<AvisoJpaEntity, UUID> {
}
