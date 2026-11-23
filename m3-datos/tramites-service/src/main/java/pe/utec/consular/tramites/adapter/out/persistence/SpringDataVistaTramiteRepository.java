package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la proyección CQRS {@code tramites.vista_tramite}.
 */
@Repository
public interface SpringDataVistaTramiteRepository extends JpaRepository<VistaTramiteJpaEntity, UUID> {

    List<VistaTramiteJpaEntity> findByDniOrderByActualizadoEnDesc(String dni);

    @Query(value = """
            SELECT v.*
            FROM tramites.vista_tramite v
            JOIN tramites.expediente e ON e.tramite_id = v.tramite_id
            WHERE e.metadatos @> CAST(:filtroJson AS jsonb)
            ORDER BY v.actualizado_en DESC
            """, nativeQuery = true)
    List<VistaTramiteJpaEntity> buscarPorMetadatosJsonb(@Param("filtroJson") String filtroJson);

    @Modifying
    @Query(value = "DELETE FROM tramites.vista_tramite", nativeQuery = true)
    void eliminarTodasLasFilas();
}

