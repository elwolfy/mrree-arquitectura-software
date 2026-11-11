package pe.utec.consular.clean.citas.entities;

import pe.utec.consular.clean.citas.entities.exception.ReglaNegocioException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad empresarial principal que modela la reserva de una cita consular.
 * <p>Concepto: Clean Architecture – Círculo 1 ({@code entities}, Reglas Empresariales, sección 1.3 del plan).
 * No tiene ninguna dependencia hacia frameworks (sin Spring, sin JPA, sin Jackson).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.domain.Cita} en {@code monolito-modular}.
 */
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
    private String notificacion;
    private final Instant creadaEn;

    public Cita(UUID id, CodigoCita codigo, Dni dni, String nombres, Email email, String oficinaId,
                UUID cupoId, LocalDate fecha, LocalTime hora, TipoTramite tipoTramite,
                EstadoCita estado, String notificacion, Instant creadaEn) {
        this.id = Objects.requireNonNull(id, "El identificador de cita no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "El código de cita no puede ser nulo");
        this.dni = Objects.requireNonNull(dni, "El DNI no puede ser nulo");
        this.nombres = Objects.requireNonNull(nombres, "Los nombres no pueden ser nulos");
        this.email = Objects.requireNonNull(email, "El correo no puede ser nulo");
        this.oficinaId = Objects.requireNonNull(oficinaId, "El identificador de oficina no puede ser nulo");
        this.cupoId = Objects.requireNonNull(cupoId, "El identificador de cupo no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.hora = Objects.requireNonNull(hora, "La hora no puede ser nula");
        this.tipoTramite = Objects.requireNonNull(tipoTramite, "El tipo de trámite no puede ser nulo");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.notificacion = Objects.requireNonNull(notificacion, "El estado de notificación no puede ser nulo");
        this.creadaEn = creadaEn != null ? creadaEn : Instant.now();
    }

    public static Cita crearReserva(CodigoCita codigo, Dni dni, String nombres, Email email,
                                    String oficinaId, UUID cupoId, LocalDate fecha, LocalTime hora,
                                    TipoTramite tipoTramite) {
        return new Cita(
                UUID.randomUUID(),
                codigo,
                dni,
                nombres,
                email,
                oficinaId,
                cupoId,
                fecha,
                hora,
                tipoTramite,
                EstadoCita.RESERVADA,
                "ENVIADA",
                Instant.now()
        );
    }

    /**
     * Cancela la cita activa validando que no esté ya cancelada.
     */
    public void cancelar() {
        if (this.estado == EstadoCita.CANCELADA) {
            throw new ReglaNegocioException("La cita " + codigo + " ya se encuentra cancelada");
        }
        this.estado = EstadoCita.CANCELADA;
    }

    public void marcarNotificacion(String nuevoEstadoNotificacion) {
        this.notificacion = nuevoEstadoNotificacion;
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

    public String getNotificacion() {
        return notificacion;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }
}

