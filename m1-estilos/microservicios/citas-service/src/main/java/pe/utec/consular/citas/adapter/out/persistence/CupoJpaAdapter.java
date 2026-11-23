package pe.utec.consular.citas.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.citas.domain.Cupo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para el puerto de salida de cupos horarios.
 */
@Component
public class CupoJpaAdapter implements CupoRepositoryPort {

    private final SpringDataCupoRepository repository;

    public CupoJpaAdapter(SpringDataCupoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Cupo> buscarPorId(UUID id) {
        return repository.findById(id).map(this::aDominio);
    }

    @Override
    public Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora) {
        return repository.buscarConBloqueo(oficinaId, fecha, hora).map(this::aDominio);
    }

    @Override
    public List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha) {
        return repository.findByOficinaIdAndFechaOrderByHoraAsc(oficinaId, fecha)
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha) {
        return repository.existsByOficinaIdAndFecha(oficinaId, fecha);
    }

    @Override
    public Cupo guardar(Cupo cupo) {
        CupoJpaEntity entity = aEntidad(cupo);
        return aDominio(repository.save(entity));
    }

    @Override
    public void guardarTodos(List<Cupo> cupos) {
        List<CupoJpaEntity> entidades = cupos.stream().map(this::aEntidad).toList();
        repository.saveAll(entidades);
    }

    private CupoJpaEntity aEntidad(Cupo c) {
        return new CupoJpaEntity(
                c.getId(),
                c.getOficinaId(),
                c.getFecha(),
                c.getHora(),
                c.getCapacidad(),
                c.getOcupados(),
                c.getVersion()
        );
    }

    private Cupo aDominio(CupoJpaEntity e) {
        return new Cupo(
                e.getId(),
                e.getOficinaId(),
                e.getFecha(),
                e.getHora(),
                e.getCapacidad(),
                e.getOcupados(),
                e.getVersion()
        );
    }
}

