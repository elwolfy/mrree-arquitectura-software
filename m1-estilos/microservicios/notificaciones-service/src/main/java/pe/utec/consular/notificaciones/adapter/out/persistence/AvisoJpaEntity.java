package pe.utec.consular.notificaciones.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la persistencia del aviso en el esquema {@code notificaciones}.
 * <p>Concepto: Aislamiento de persistencia en arquitectura hexagonal (regla 01).
 */
@Entity
@Table(name = "aviso", schema = "notificaciones")
public class AvisoJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String destinatario;

    @Column(nullable = false, length = 160)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String cuerpo;

    @Column(nullable = false, length = 10)
    private String canal;

    @Column(nullable = false, length = 10)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(length = 40)
    private String referencia;

    public AvisoJpaEntity() {
    }

    public AvisoJpaEntity(UUID id, String destinatario, String asunto, String cuerpo, String canal,
                          String estado, Instant creadoEn, String referencia) {
        this.id = id;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.cuerpo = cuerpo;
        this.canal = canal;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.referencia = referencia;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}

