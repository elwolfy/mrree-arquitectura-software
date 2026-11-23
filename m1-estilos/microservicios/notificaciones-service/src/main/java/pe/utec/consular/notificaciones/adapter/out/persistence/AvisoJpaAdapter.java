package pe.utec.consular.notificaciones.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.notificaciones.application.port.out.AvisoRepositoryPort;
import pe.utec.consular.notificaciones.domain.Aviso;
import pe.utec.consular.notificaciones.domain.CanalNotificacion;
import pe.utec.consular.notificaciones.domain.EstadoNotificacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia JPA para el puerto de salida de avisos.
 * <p>Concepto: Adaptador secundario en arquitectura hexagonal (sección 1.3 del plan).
 */
@Component
public class AvisoJpaAdapter implements AvisoRepositoryPort {

    private final SpringDataAvisoRepository repository;

    public AvisoJpaAdapter(SpringDataAvisoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Aviso guardar(Aviso aviso) {
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
        return aDominio(repository.save(entity));
    }

    @Override
    public Optional<Aviso> buscarPorId(UUID id) {
        return repository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Aviso> buscarPorReferencia(String referencia) {
        return repository.findByReferenciaOrderByCreadoEnDesc(referencia)
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<Aviso> listarTodos() {
        return repository.findAll()
                .stream()
                .map(this::aDominio)
                .toList();
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

