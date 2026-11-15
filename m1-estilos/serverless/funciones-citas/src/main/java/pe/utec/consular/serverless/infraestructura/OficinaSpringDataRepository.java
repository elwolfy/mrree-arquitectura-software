package pe.utec.consular.serverless.infraestructura;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OficinaSpringDataRepository extends JpaRepository<OficinaJpaEntity, String> {

    List<OficinaJpaEntity> findAllByOrderByIdAsc();
}

