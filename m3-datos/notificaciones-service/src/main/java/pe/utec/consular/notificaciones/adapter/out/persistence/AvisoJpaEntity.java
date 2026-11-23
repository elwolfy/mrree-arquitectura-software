package pe.utec.consular.notificaciones.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code notificaciones.aviso}.
 */
@Entity
@Table(name = "aviso", schema = "notificaciones")
public class AvisoJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 8)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String dni;

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

    @Column(nullable = false, length = 40)
    private String referencia;

    public AvisoJpaEntity() {
    }

    public AvisoJpaEntity(UUID id,
                          String dni,
                          String destinatario,
                          String asunto,
                          String cuerpo,
                          String canal,
                          String estado,
                          Instant creadoEn,
                          String referencia) {
        this.id = id;
        this.dni = dni;
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

    public String getDni() {
        return dni;
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

    public String getCanal() {
        return canal;
    }

    public String getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public String getReferencia() {
        return referencia;
    }
}

