package pe.utec.consular.serverless.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public class Cita {

    private final UUID id;
    private final CodigoCita codigo;
    private final Dni dni;
    private final String nombres;
    private final Email email;
    private final String oficinaId;
    private final UUID cupoId;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final TipoTramite tipoTramite;
    private EstadoCita estado;
    private EstadoNotificacion notificacion;
    private final Instant creadaEn;

    public Cita(
            UUID id,
            CodigoCita codigo,
            Dni dni,
            String nombres,
            Email email,
            String oficinaId,
            UUID cupoId,
            LocalDate fecha,
            LocalTime hora,
            TipoTramite tipoTramite,
            EstadoCita estado,
            EstadoNotificacion notificacion,
            Instant creadaEn
    ) {
        this.id = Objects.requireNonNull(id, "El ID de la cita no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "El código de la cita no puede ser nulo");
        this.dni = Objects.requireNonNull(dni, "El DNI no puede ser nulo");
        if (nombres == null || nombres.trim().length() < 3 || nombres.trim().length() > 120) {
            throw new IllegalArgumentException("El nombre debe tener entre 3 y 120 caracteres");
        }
        this.nombres = nombres.trim();
        this.email = Objects.requireNonNull(email, "El correo electrónico no puede ser nulo");
        this.oficinaId = Objects.requireNonNull(oficinaId, "El ID de la oficina no puede ser nulo");
        this.cupoId = Objects.requireNonNull(cupoId, "El ID del cupo no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.hora = Objects.requireNonNull(hora, "La hora no puede ser nula");
        this.tipoTramite = Objects.requireNonNull(tipoTramite, "El tipo de trámite no puede ser nulo");
        this.estado = Objects.requireNonNull(estado, "El estado de la cita no puede ser nulo");
        this.notificacion = Objects.requireNonNull(notificacion, "El estado de notificación no puede ser nulo");
        this.creadaEn = Objects.requireNonNull(creadaEn, "La fecha de creación no puede ser nula");
    }

    public static Cita crearNueva(
            CodigoCita codigo,
            Dni dni,
            String nombres,
            Email email,
            String oficinaId,
            Cupo cupo,
            TipoTramite tipoTramite
    ) {
        return new Cita(
                UUID.randomUUID(),
                codigo,
                dni,
                nombres,
                email,
                oficinaId,
                cupo.getId(),
                cupo.getFecha(),
                cupo.getHora(),
                tipoTramite,
                EstadoCita.RESERVADA,
                EstadoNotificacion.PENDIENTE,
                Instant.now()
        );
    }

    public void cancelar() {
        if (this.estado == EstadoCita.ATENDIDA || this.estado == EstadoCita.CANCELADA) {
            throw new TransicionEstadoInvalidaException(this.id, this.estado, EstadoCita.CANCELADA);
        }
        this.estado = EstadoCita.CANCELADA;
    }

    public void actualizarEstadoNotificacion(EstadoNotificacion nuevoEstado) {
        this.notificacion = Objects.requireNonNull(nuevoEstado);
    }

    public UUID getId() {
        return id;
    }

    public CodigoCita getCodigo() {
        return codigo;
    }

    public Dni getDni() {
        return dni;
    }

    public String getNombres() {
        return nombres;
    }

    public Email getEmail() {
        return email;
    }

    public String getOficinaId() {
        return oficinaId;
    }

    public UUID getCupoId() {
        return cupoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public TipoTramite getTipoTramite() {
        return tipoTramite;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public EstadoNotificacion getNotificacion() {
        return notificacion;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }
}

