package pe.utec.consular.serverless.infraestructura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "aviso", schema = "notificaciones")
public class AvisoJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "destinatario", length = 120, nullable = false)
    private String destinatario;

    @Column(name = "asunto", length = 160, nullable = false)
    private String asunto;

    @Column(name = "cuerpo", columnDefinition = "TEXT", nullable = false)
    private String cuerpo;

    @Column(name = "canal", length = 10, nullable = false)
    private String canal;

    @Column(name = "estado", length = 10, nullable = false)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "referencia", length = 40)
    private String referencia;

    protected AvisoJpaEntity() {
    }

    public AvisoJpaEntity(
            UUID id,
            String destinatario,
            String asunto,
            String cuerpo,
            String canal,
            String estado,
            Instant creadoEn,
            String referencia
    ) {
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

    public Instant getCreadaEn() {
        return creadoEn;
    }

    public String getReferencia() {
        return referencia;
    }
}

