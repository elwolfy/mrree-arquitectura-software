package pe.utec.consular.monolito.notificaciones.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.notificaciones.application.port.out.AvisoRepositoryPort;
import pe.utec.consular.monolito.notificaciones.domain.Aviso;
import pe.utec.consular.monolito.notificaciones.domain.CanalNotificacion;
import pe.utec.consular.monolito.notificaciones.domain.EstadoNotificacion;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia JPA para el puerto de salida de avisos.
 * <p>Concepto: Adaptador secundario / de salida en arquitectura hexagonal (sección 1.3).
 * <p>Sesión: S1. Mapeo entre entidades JPA y el modelo de dominio puro.
 */
@Component
public class AvisoJpaAdapter implements AvisoRepositoryPort {

    private final SpringDataAvisoRepository repository;

    public AvisoJpaAdapter(SpringDataAvisoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardar(Aviso aviso) {
        AvisoJpaEntity entity = new AvisoJpaEntity(
                aviso.getId(),
                aviso.getDestinatario(),
                aviso.getAsunto(),
                aviso.getCuerpo(),
                aviso.getCanal().name(),
                aviso.getEstado().name(),
                aviso.getCreadoEn(),
                aviso.getReferencia()
        );
        repository.save(entity);
    }

    @Override
    public Optional<Aviso> buscarPorId(UUID id) {
        return repository.findById(id).map(this::aDominio);
    }

    private Aviso aDominio(AvisoJpaEntity entity) {
        return new Aviso(
                entity.getId(),
                entity.getDestinatario(),
                entity.getAsunto(),
                entity.getCuerpo(),
                CanalNotificacion.valueOf(entity.getCanal()),
                EstadoNotificacion.valueOf(entity.getEstado()),
                entity.getCreadoEn(),
                entity.getReferencia()
        );
    }
}
