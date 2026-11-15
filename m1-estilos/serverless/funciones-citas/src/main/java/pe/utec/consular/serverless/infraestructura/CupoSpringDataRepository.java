package pe.utec.consular.serverless.infraestructura;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CupoSpringDataRepository extends JpaRepository<CupoJpaEntity, UUID> {

    List<CupoJpaEntity> findByOficinaIdAndFechaOrderByHoraAsc(String oficinaId, LocalDate fecha);

    Optional<CupoJpaEntity> findByOficinaIdAndFechaAndHora(String oficinaId, LocalDate fecha, LocalTime hora);
}

