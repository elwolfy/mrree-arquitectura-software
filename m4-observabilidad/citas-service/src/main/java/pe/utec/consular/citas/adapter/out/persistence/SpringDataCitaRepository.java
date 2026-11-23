package pe.utec.consular.citas.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para citas consulares.
 */
@Repository
public interface SpringDataCitaRepository extends JpaRepository<CitaJpaEntity, UUID> {

    List<CitaJpaEntity> findByDni(String dni);

    @Query("SELECT COUNT(c) > 0 FROM CitaJpaEntity c WHERE c.dni = :dni AND c.oficinaId = :oficinaId AND c.cupo.fecha = :fecha AND c.estado = :estado")
    boolean existsByDniAndOficinaIdAndFechaAndEstado(@Param("dni") String dni,
                                                     @Param("oficinaId") String oficinaId,
                                                     @Param("fecha") LocalDate fecha,
                                                     @Param("estado") String estado);

    @Query(value = "SELECT nextval('citas.cita_codigo_seq')", nativeQuery = true)
    Long obtenerSiguienteSecuencia();
}

