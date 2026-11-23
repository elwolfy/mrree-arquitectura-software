package pe.utec.consular.monolito.citas.adapter.out.persistence;

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
 * Repositorio Spring Data JPA para cupos con soporte de bloqueo pesimista.
 * <p>Concepto: Bloqueo pesimista PESSIMISTIC_WRITE (regla 02 y sección 4.2).
 */
@Repository
public interface SpringDataCupoRepository extends JpaRepository<CupoJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CupoJpaEntity c WHERE c.oficinaId = :oficinaId AND c.fecha = :fecha AND c.hora = :hora")
    Optional<CupoJpaEntity> buscarConBloqueoPesimista(
            @Param("oficinaId") String oficinaId,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );

    List<CupoJpaEntity> findByOficinaIdAndFecha(String oficinaId, LocalDate fecha);

    List<CupoJpaEntity> findByOficinaIdAndFechaBetween(String oficinaId, LocalDate desde, LocalDate hasta);

    boolean existsByOficinaIdAndFecha(String oficinaId, LocalDate fecha);
}
