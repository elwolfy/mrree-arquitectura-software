package pe.utec.consular.emision.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@code emision.documento_emitido}.
 */
@Repository
public interface SpringDataDocumentoEmitidoRepository extends JpaRepository<DocumentoEmitidoJpaEntity, UUID> {

    @Query(value = "SELECT nextval('emision.documento_seq')", nativeQuery = true)
    Long obtenerSiguienteSecuencia();
}

