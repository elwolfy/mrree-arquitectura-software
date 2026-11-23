package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.out.TramiteRepositoryPort;
import pe.utec.consular.tramites.domain.CodigoTramite;
import pe.utec.consular.tramites.domain.Dni;
import pe.utec.consular.tramites.domain.Email;
import pe.utec.consular.tramites.domain.EstadoTramite;
import pe.utec.consular.tramites.domain.TipoTramite;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.TramiteFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para el puerto de persistencia de trámites consulares.
 */
@Component
public class TramiteJpaAdapter implements TramiteRepositoryPort {

    private final SpringDataTramiteRepository repository;

    public TramiteJpaAdapter(SpringDataTramiteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Tramite guardar(Tramite tramite) {
        TramiteJpaEntity entity = new TramiteJpaEntity(
                tramite.getId(),
                tramite.getCodigo().valor(),
                tramite.getDni().valor(),
                tramite.getNombres(),
                tramite.getEmail().valor(),
                tramite.getTipoTramite().name(),
                tramite.getSubtipo(),
                tramite.getPais(),
                tramite.getFolios(),
                tramite.getIdioma(),
                tramite.getEstado().name(),
                tramite.getMonto(),
                tramite.getCreadoEn()
        );
        TramiteJpaEntity guardado = repository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public Optional<Tramite> buscarPorId(UUID id) {
        return repository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Tramite> buscarPorDni(String dni) {
        return repository.findByDniOrderByCreadoEnDesc(dni).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public long siguienteSecuencia() {
        Long seq = repository.obtenerSiguienteSecuencia();
        return seq != null ? seq : 1L;
    }

    private Tramite aDominio(TramiteJpaEntity entity) {
        return TramiteFactory.reconstruir(
                entity.getId(),
                new CodigoTramite(entity.getCodigo()),
                new Dni(entity.getDni()),
                entity.getNombres(),
                new Email(entity.getEmail()),
                TipoTramite.valueOf(entity.getTipoTramite()),
                entity.getPais(),
                entity.getFolios(),
                entity.getIdioma(),
                EstadoTramite.valueOf(entity.getEstado()),
                entity.getMonto(),
                entity.getCreadoEn()
        );
    }
}

