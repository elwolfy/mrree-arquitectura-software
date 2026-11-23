package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para trámites consulares.
 */
@Repository
public interface SpringDataTramiteRepository extends JpaRepository<TramiteJpaEntity, UUID> {

    List<TramiteJpaEntity> findByDniOrderByCreadoEnDesc(String dni);

    @Query(value = "SELECT nextval('tramites.tramite_codigo_seq')", nativeQuery = true)
    Long obtenerSiguienteSecuencia();
}

