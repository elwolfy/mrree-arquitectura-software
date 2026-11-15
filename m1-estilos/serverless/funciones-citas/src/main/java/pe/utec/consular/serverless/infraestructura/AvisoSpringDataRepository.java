package pe.utec.consular.serverless.infraestructura;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvisoSpringDataRepository extends JpaRepository<AvisoJpaEntity, UUID> {
}

