package pe.utec.consular.citas.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.citas.domain.Cita;
import pe.utec.consular.citas.domain.CodigoCita;
import pe.utec.consular.citas.domain.Dni;
import pe.utec.consular.citas.domain.Email;
import pe.utec.consular.citas.domain.EstadoCita;
import pe.utec.consular.citas.domain.TipoTramite;
import pe.utec.consular.citas.domain.exception.RecursoNoEncontradoException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para el puerto de salida de citas consulares.
 */
@Component
public class CitaJpaAdapter implements CitaRepositoryPort {

    private final SpringDataCitaRepository citaRepository;
    private final SpringDataCupoRepository cupoRepository;

    public CitaJpaAdapter(SpringDataCitaRepository citaRepository,
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
        return aDominio(citaRepository.save(entity));
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

