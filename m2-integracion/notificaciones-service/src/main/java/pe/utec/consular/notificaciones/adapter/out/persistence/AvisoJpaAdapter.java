package pe.utec.consular.notificaciones.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.notificaciones.application.port.out.AvisoRepositoryPort;
import pe.utec.consular.notificaciones.domain.Aviso;

import java.util.List;

/**
 * Adaptador JPA para el repositorio de avisos.
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
                aviso.id(),
                aviso.dni(),
                aviso.destinatario(),
                aviso.asunto(),
                aviso.cuerpo(),
                aviso.canal(),
                aviso.estado(),
                aviso.creadoEn(),
                aviso.referencia()
        );
        AvisoJpaEntity guardado = repository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public List<Aviso> buscarPorDni(String dni) {
        return repository.findByDniOrderByCreadoEnDesc(dni).stream()
                .map(this::aDominio)
                .toList();
    }

    private Aviso aDominio(AvisoJpaEntity entity) {
        return new Aviso(
                entity.getId(),
                entity.getDni(),
                entity.getDestinatario(),
                entity.getAsunto(),
                entity.getCuerpo(),
                entity.getCanal(),
                entity.getEstado(),
                entity.getCreadoEn(),
                entity.getReferencia()
        );
    }
}

