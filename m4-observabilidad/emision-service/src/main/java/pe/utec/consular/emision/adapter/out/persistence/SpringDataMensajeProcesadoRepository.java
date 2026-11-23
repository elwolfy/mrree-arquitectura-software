package pe.utec.consular.emision.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para {@code emision.mensaje_procesado}.
 */
@Repository
public interface SpringDataMensajeProcesadoRepository extends JpaRepository<MensajeProcesadoJpaEntity, String> {
}

