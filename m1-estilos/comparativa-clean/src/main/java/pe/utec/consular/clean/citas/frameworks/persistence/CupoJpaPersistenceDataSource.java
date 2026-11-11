package pe.utec.consular.clean.citas.frameworks.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.clean.citas.entities.Cupo;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.CupoPersistenceDataSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA del origen de datos de cupos en {@code frameworks.persistence}.
 */
@Component
public class CupoJpaPersistenceDataSource implements CupoPersistenceDataSource {

    private final SpringDataCupoRepository repository;

    public CupoJpaPersistenceDataSource(SpringDataCupoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora) {
        return repository.buscarConBloqueoPesimista(oficinaId, fecha, hora).map(this::aDominio);
    }

    @Override
    public List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha) {
        return repository.findByOficinaIdAndFecha(oficinaId, fecha).stream().map(this::aDominio).toList();
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
        repository.saveAll(cupos.stream().map(this::aEntidad).toList());
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
}

