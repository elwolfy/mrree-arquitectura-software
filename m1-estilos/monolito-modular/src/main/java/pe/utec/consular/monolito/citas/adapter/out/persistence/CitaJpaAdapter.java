package pe.utec.consular.monolito.citas.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cita;
import pe.utec.consular.monolito.citas.domain.CodigoCita;
import pe.utec.consular.monolito.citas.domain.EstadoCita;
import pe.utec.consular.monolito.compartido.domain.Dni;
import pe.utec.consular.monolito.compartido.domain.Email;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para el puerto de persistencia de citas.
 * <p>Concepto: Adaptador de salida en arquitectura hexagonal (sección 1.3).
 * <p>Sesión: S1. Mapea la entidad JPA al modelo puro de dominio.
 */
@Component
public class CitaJpaAdapter implements CitaRepositoryPort {

    private final SpringDataCitaRepository repository;
    private final SpringDataCupoRepository cupoRepository;

    public CitaJpaAdapter(SpringDataCitaRepository repository, SpringDataCupoRepository cupoRepository) {
        this.repository = repository;
        this.cupoRepository = cupoRepository;
    }

    @Override
    public Cita guardar(Cita cita) {
        CupoJpaEntity cupoEntity = cupoRepository.getReferenceById(cita.getCupoId());
        CitaJpaEntity entity = new CitaJpaEntity(
                cita.getId(),
                cita.getCodigo().valor(),
                cita.getDni().valor(),
                cita.getNombres(),
                cita.getEmail().valor(),
                cita.getOficinaId(),
                cupoEntity,
                cita.getTipoTramite().name(),
                cita.getEstado().name(),
                cita.getNotificacion(),
                cita.getCreadaEn()
        );
        CitaJpaEntity guardado = repository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public Optional<Cita> buscarPorId(UUID id) {
        return repository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Cita> buscarPorDni(String dni) {
        return repository.findByDni(dni).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public boolean existeCitaActiva(String dni, String oficinaId, LocalDate fecha) {
        return repository.existsByDniAndOficinaIdAndFechaAndEstado(
                dni, oficinaId, fecha, EstadoCita.RESERVADA.name()
        );
    }

    @Override
    public long siguienteSecuencia() {
        Long seq = repository.obtenerSiguienteSecuencia();
        return seq != null ? seq : 1L;
    }

    private Cita aDominio(CitaJpaEntity entity) {
        return new Cita(
                entity.getId(),
                new CodigoCita(entity.getCodigo()),
                new Dni(entity.getDni()),
                entity.getNombres(),
                new Email(entity.getEmail()),
                entity.getOficinaId(),
                entity.getCupoId(),
                entity.getFecha(),
                entity.getHora(),
                TipoTramite.valueOf(entity.getTipoTramite()),
                EstadoCita.valueOf(entity.getEstado()),
                entity.getNotificacion(),
                entity.getCreadaEn()
        );
    }
}
