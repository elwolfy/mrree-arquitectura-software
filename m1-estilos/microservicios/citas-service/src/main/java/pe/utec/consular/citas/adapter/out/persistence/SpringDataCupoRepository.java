package pe.utec.consular.citas.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para cupos horarios con bloqueo pesimista ({@code SELECT ... FOR UPDATE}).
 */
@Repository
public interface SpringDataCupoRepository extends JpaRepository<CupoJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CupoJpaEntity c WHERE c.oficinaId = :oficinaId AND c.fecha = :fecha AND c.hora = :hora")
    Optional<CupoJpaEntity> buscarConBloqueo(@Param("oficinaId") String oficinaId,
                                             @Param("fecha") LocalDate fecha,
                                             @Param("hora") LocalTime hora);

    List<CupoJpaEntity> findByOficinaIdAndFechaOrderByHoraAsc(String oficinaId, LocalDate fecha);

    boolean existsByOficinaIdAndFecha(String oficinaId, LocalDate fecha);
}

