package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para el Event Store {@code tramites.eventos_tramite}.
 * Solo ejecuta inserciones y lecturas (nunca UPDATE ni DELETE).
 */
@Repository
public interface SpringDataEventoTramiteRepository extends JpaRepository<EventoTramiteJpaEntity, Long> {

    List<EventoTramiteJpaEntity> findByTramiteIdOrderByVersionAsc(UUID tramiteId);

    @Query("SELECT COALESCE(MAX(e.version), 0) FROM EventoTramiteJpaEntity e WHERE e.tramiteId = :tramiteId")
    int obtenerUltimaVersion(@Param("tramiteId") UUID tramiteId);

    @Query("SELECT e FROM EventoTramiteJpaEntity e ORDER BY e.tramiteId ASC, e.version ASC")
    List<EventoTramiteJpaEntity> listarTodosOrdenadosPorTramiteYVersion();

    @Query(value = "SELECT nextval('tramites.tramite_codigo_seq')", nativeQuery = true)
    Long obtenerSiguienteSecuencia();
}

