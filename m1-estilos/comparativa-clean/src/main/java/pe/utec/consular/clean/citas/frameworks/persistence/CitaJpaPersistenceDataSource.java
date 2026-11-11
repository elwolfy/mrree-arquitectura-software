package pe.utec.consular.clean.citas.frameworks.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.clean.citas.entities.Cita;
import pe.utec.consular.clean.citas.entities.CodigoCita;
import pe.utec.consular.clean.citas.entities.Dni;
import pe.utec.consular.clean.citas.entities.Email;
import pe.utec.consular.clean.citas.entities.EstadoCita;
import pe.utec.consular.clean.citas.entities.TipoTramite;
import pe.utec.consular.clean.citas.entities.exception.RecursoNoEncontradoException;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.CitaPersistenceDataSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación JPA del origen de datos de citas en {@code frameworks.persistence}.
 * <p>Concepto: Clean Architecture – Círculo 4 ({@code frameworks.persistence}) implementando la interfaz
 * de {@code interfaceadapters.gateway}.
 */
@Component
public class CitaJpaPersistenceDataSource implements CitaPersistenceDataSource {

    private final SpringDataCitaRepository citaRepository;
    private final SpringDataCupoRepository cupoRepository;

    public CitaJpaPersistenceDataSource(SpringDataCitaRepository citaRepository,
                                        SpringDataCupoRepository cupoRepository) {
        this.citaRepository = citaRepository;
        this.cupoRepository = cupoRepository;
    }

    @Override
    public Cita guardar(Cita cita) {
        CupoJpaEntity cupoEntity = cupoRepository.findById(cita.getCupoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el cupo asociado a la cita: " + cita.getCupoId()));

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
        CitaJpaEntity guardada = citaRepository.save(entity);
        return aDominio(guardada);
    }

    @Override
    public Optional<Cita> buscarPorId(UUID id) {
        return citaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Cita> buscarPorDni(String dni) {
        return citaRepository.findByDni(dni).stream().map(this::aDominio).toList();
    }

    @Override
    public boolean existeCitaActiva(String dni, String oficinaId, LocalDate fecha) {
        return citaRepository.existsByDniAndOficinaIdAndFechaAndEstado(
                dni, oficinaId, fecha, EstadoCita.RESERVADA.name());
    }

    @Override
    public long siguienteSecuencia() {
        return citaRepository.obtenerSiguienteSecuencia();
    }

    private Cita aDominio(CitaJpaEntity e) {
        return new Cita(
                e.getId(),
                new CodigoCita(e.getCodigo()),
                new Dni(e.getDni()),
                e.getNombres(),
                new Email(e.getEmail()),
                e.getOficinaId(),
                e.getCupoId(),
                e.getFecha(),
                e.getHora(),
                TipoTramite.valueOf(e.getTipoTramite()),
                EstadoCita.valueOf(e.getEstado()),
                e.getNotificacion(),
                e.getCreadaEn()
        );
    }
}

