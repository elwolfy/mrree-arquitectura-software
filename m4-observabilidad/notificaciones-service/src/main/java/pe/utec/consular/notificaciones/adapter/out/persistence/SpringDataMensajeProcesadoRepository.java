package pe.utec.consular.notificaciones.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para el registro de mensajes procesados.
 */
@Repository
public interface SpringDataMensajeProcesadoRepository extends JpaRepository<MensajeProcesadoJpaEntity, UUID> {
}

