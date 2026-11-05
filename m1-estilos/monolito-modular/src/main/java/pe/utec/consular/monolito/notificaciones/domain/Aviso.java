package pe.utec.consular.monolito.notificaciones.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un aviso o notificación cursada a un ciudadano.
 * <p>Concepto: Entidad de dominio pura sin dependencias de infraestructura (regla 01).
 * <p>Sesión: S1.
 */
public class Aviso {

    private final UUID id;
    private final String destinatario;
    private final String asunto;
    private final String cuerpo;
    private final CanalNotificacion canal;
    private EstadoNotificacion estado;
    private final Instant creadoEn;
    private final String referencia;

    public Aviso(UUID id, String destinatario, String asunto, String cuerpo, CanalNotificacion canal,
                 EstadoNotificacion estado, Instant creadoEn, String referencia) {
        this.id = Objects.requireNonNull(id, "El identificador no puede ser nulo");
        this.destinatario = Objects.requireNonNull(destinatario, "El destinatario no puede ser nulo");
        this.asunto = Objects.requireNonNull(asunto, "El asunto no puede ser nulo");
        this.cuerpo = Objects.requireNonNull(cuerpo, "El cuerpo no puede ser nulo");
        this.canal = Objects.requireNonNull(canal, "El canal no puede ser nulo");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.creadoEn = creadoEn != null ? creadoEn : Instant.now();
        this.referencia = referencia;
    }

    public static Aviso nuevo(String destinatario, String asunto, String cuerpo, CanalNotificacion canal, String referencia) {
        return new Aviso(
                UUID.randomUUID(),
                destinatario,
                asunto,
                cuerpo,
                canal,
                EstadoNotificacion.ENVIADA,
                Instant.now(),
                referencia
        );
    }

    public void marcarComoFallida() {
        this.estado = EstadoNotificacion.FALLIDA;
    }

    public UUID getId() {
        return id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public CanalNotificacion getCanal() {
        return canal;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public String getReferencia() {
        return referencia;
    }
}
