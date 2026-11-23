package pe.utec.consular.notificaciones.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para avisos en el esquema {@code notificaciones}.
 */
@Repository
public interface SpringDataAvisoRepository extends JpaRepository<AvisoJpaEntity, UUID> {

    List<AvisoJpaEntity> findByReferenciaOrderByCreadoEnDesc(String referencia);
}

