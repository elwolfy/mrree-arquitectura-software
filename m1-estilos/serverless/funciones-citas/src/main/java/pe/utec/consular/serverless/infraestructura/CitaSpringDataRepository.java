package pe.utec.consular.serverless.infraestructura;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CitaSpringDataRepository extends JpaRepository<CitaJpaEntity, UUID> {

    List<CitaJpaEntity> findByDniOrderByCreadaEnDesc(String dni);

    boolean existsByDniAndTipoTramiteAndEstadoIn(String dni, String tipoTramite, List<String> estados);

    @Query(value = "SELECT nextval('citas.cita_codigo_seq')", nativeQuery = true)
    long siguienteCorrelativo();
}

