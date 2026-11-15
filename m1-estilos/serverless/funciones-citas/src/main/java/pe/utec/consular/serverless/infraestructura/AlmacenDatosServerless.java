package pe.utec.consular.serverless.infraestructura;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.serverless.dominio.Aviso;
import pe.utec.consular.serverless.dominio.CanalNotificacion;
import pe.utec.consular.serverless.dominio.Cita;
import pe.utec.consular.serverless.dominio.CodigoCita;
import pe.utec.consular.serverless.dominio.Cupo;
import pe.utec.consular.serverless.dominio.Dni;
import pe.utec.consular.serverless.dominio.Email;
import pe.utec.consular.serverless.dominio.EstadoCita;
import pe.utec.consular.serverless.dominio.EstadoNotificacion;
import pe.utec.consular.serverless.dominio.OficinaConsular;
import pe.utec.consular.serverless.dominio.TipoTramite;

@Component
public class AlmacenDatosServerless {

    private final OficinaSpringDataRepository oficinaRepository;
    private final CupoSpringDataRepository cupoRepository;
    private final CitaSpringDataRepository citaRepository;
    private final AvisoSpringDataRepository avisoRepository;
    private final GeneradorDeCupos generadorDeCupos;

    public AlmacenDatosServerless(
            OficinaSpringDataRepository oficinaRepository,
            CupoSpringDataRepository cupoRepository,
            CitaSpringDataRepository citaRepository,
            AvisoSpringDataRepository avisoRepository,
            GeneradorDeCupos generadorDeCupos
    ) {
        this.oficinaRepository = oficinaRepository;
        this.cupoRepository = cupoRepository;
        this.citaRepository = citaRepository;
        this.avisoRepository = avisoRepository;
        this.generadorDeCupos = generadorDeCupos;
    }

    @Transactional(readOnly = true)
    public List<OficinaConsular> listarOficinas() {
        return oficinaRepository.findAllByOrderByIdAsc().stream()
                .map(this::aDominio)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<OficinaConsular> buscarOficinaPorId(String id) {
        return oficinaRepository.findById(id).map(this::aDominio);
    }

    @Transactional
    public List<Cupo> obtenerCupos(String oficinaId, LocalDate fecha) {
        return generadorDeCupos.obtenerOGenerarCupos(oficinaId, fecha);
    }

    @Transactional
    public Optional<Cupo> buscarYObtenerCupo(String oficinaId, LocalDate fecha, LocalTime hora) {
        generadorDeCupos.obtenerOGenerarCupos(oficinaId, fecha);
        return cupoRepository.findByOficinaIdAndFechaAndHora(oficinaId, fecha, hora)
                .map(this::aDominio);
    }

    @Transactional(readOnly = true)
    public Optional<Cupo> buscarCupoPorId(UUID cupoId) {
        return cupoRepository.findById(cupoId).map(this::aDominio);
    }

    @Transactional
    public Cupo guardarCupo(Cupo cupo) {
        CupoJpaEntity entity = new CupoJpaEntity(
                cupo.getId(),
                cupo.getOficinaId(),
                cupo.getFecha(),
                cupo.getHora(),
                cupo.getCapacidad(),
                cupo.getOcupados(),
                cupo.getVersion()
        );
        return aDominio(cupoRepository.saveAndFlush(entity));
    }

    @Transactional
    public Cita guardarCita(Cita cita) {
        CitaJpaEntity entity = new CitaJpaEntity(
                cita.getId(),
                cita.getCodigo().valor(),
                cita.getDni().valor(),
                cita.getNombres(),
                cita.getEmail().valor(),
                cita.getOficinaId(),
                cita.getCupoId(),
                cita.getTipoTramite().name(),
                cita.getEstado().name(),
                cita.getNotificacion().name(),
                cita.getCreadaEn()
        );
        citaRepository.saveAndFlush(entity);
        return cita;
    }

    @Transactional(readOnly = true)
    public Optional<Cita> buscarCitaPorId(UUID id) {
        return citaRepository.findById(id).map(this::aDominio);
    }

    @Transactional(readOnly = true)
    public List<Cita> buscarCitasPorDni(Dni dni) {
        return citaRepository.findByDniOrderByCreadaEnDesc(dni.valor()).stream()
                .map(this::aDominio)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existeCitaActiva(Dni dni, TipoTramite tipoTramite) {
        return citaRepository.existsByDniAndTipoTramiteAndEstadoIn(
                dni.valor(),
                tipoTramite.name(),
                List.of(EstadoCita.RESERVADA.name(), EstadoCita.CONFIRMADA.name())
        );
    }

    @Transactional
    public long siguienteCorrelativoCodigo() {
        return citaRepository.siguienteCorrelativo();
    }

    @Transactional
    public Aviso guardarAviso(Aviso aviso) {
        AvisoJpaEntity entity = new AvisoJpaEntity(
                aviso.id(),
                aviso.destinatario(),
                aviso.asunto(),
                aviso.cuerpo(),
                aviso.canal().name(),
                aviso.estado().name(),
                aviso.creadoEn(),
                aviso.referencia()
        );
        avisoRepository.save(entity);
        return aviso;
    }

    private OficinaConsular aDominio(OficinaJpaEntity e) {
        return new OficinaConsular(e.getId(), e.getNombre(), e.getPais(), e.getCiudad(), e.getZonaHoraria());
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

    private Cita aDominio(CitaJpaEntity e) {
        CupoJpaEntity cupo = cupoRepository.findById(e.getCupoId())
                .orElseThrow(() -> new IllegalStateException("Cupo no encontrado para la cita: " + e.getId()));
        return new Cita(
                e.getId(),
                new CodigoCita(e.getCodigo()),
                new Dni(e.getDni()),
                e.getNombres(),
                new Email(e.getEmail()),
                e.getOficinaId(),
                e.getCupoId(),
                cupo.getFecha(),
                cupo.getHora(),
                TipoTramite.valueOf(e.getTipoTramite()),
                EstadoCita.valueOf(e.getEstado()),
                EstadoNotificacion.valueOf(e.getNotificacion()),
                e.getCreadaEn()
        );
    }
}

