package pe.utec.consular.monolito.citas.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cupo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA para el puerto de persistencia de cupos consulares.
 * <p>Concepto: Adaptador secundario hexagonal con bloqueo pesimista (secciones 1.1 y 1.3).
 */
@Component
public class CupoJpaAdapter implements CupoRepositoryPort {

    private final SpringDataCupoRepository repository;

    public CupoJpaAdapter(SpringDataCupoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora) {
        return repository.buscarConBloqueoPesimista(oficinaId, fecha, hora).map(this::aDominio);
    }

    @Override
    public List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha) {
        return repository.findByOficinaIdAndFecha(oficinaId, fecha).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<Cupo> buscarPorOficinaYFechaEntre(String oficinaId, LocalDate desde, LocalDate hasta) {
        return repository.findByOficinaIdAndFechaBetween(oficinaId, desde, hasta).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Cupo guardar(Cupo cupo) {
        CupoJpaEntity entity = new CupoJpaEntity(
                cupo.getId(),
                cupo.getOficinaId(),
                cupo.getFecha(),
                cupo.getHora(),
                cupo.getCapacidad(),
                cupo.getOcupados(),
                cupo.getVersion()
        );
        CupoJpaEntity guardado = repository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public void guardarTodos(List<Cupo> cupos) {
        List<CupoJpaEntity> entities = cupos.stream()
                .map(c -> new CupoJpaEntity(
                        c.getId(),
                        c.getOficinaId(),
                        c.getFecha(),
                        c.getHora(),
                        c.getCapacidad(),
                        c.getOcupados(),
                        c.getVersion()))
                .toList();
        repository.saveAll(entities);
    }

    @Override
    public boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha) {
        return repository.existsByOficinaIdAndFecha(oficinaId, fecha);
    }

    private Cupo aDominio(CupoJpaEntity entity) {
        return new Cupo(
                entity.getId(),
                entity.getOficinaId(),
                entity.getFecha(),
                entity.getHora(),
                entity.getCapacidad(),
                entity.getOcupados(),
                entity.getVersion()
        );
    }
}
