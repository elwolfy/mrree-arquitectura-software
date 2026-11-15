package pe.utec.consular.serverless.infraestructura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cita", schema = "citas")
public class CitaJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "dni", length = 8, nullable = false)
    private String dni;

    @Column(name = "nombres", length = 120, nullable = false)
    private String nombres;

    @Column(name = "email", length = 120, nullable = false)
    private String email;

    @Column(name = "oficina_id", length = 5, nullable = false)
    private String oficinaId;

    @Column(name = "cupo_id", nullable = false)
    private UUID cupoId;

    @Column(name = "tipo_tramite", length = 20, nullable = false)
    private String tipoTramite;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "notificacion", length = 20, nullable = false)
    private String notificacion;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    protected CitaJpaEntity() {
    }

    public CitaJpaEntity(
            UUID id,
            String codigo,
            String dni,
            String nombres,
            String email,
            String oficinaId,
            UUID cupoId,
            String tipoTramite,
            String estado,
            String notificacion,
            Instant creadaEn
    ) {
        this.id = id;
        this.codigo = codigo;
        this.dni = dni;
        this.nombres = nombres;
        this.email = email;
        this.oficinaId = oficinaId;
        this.cupoId = cupoId;
        this.tipoTramite = tipoTramite;
        this.estado = estado;
        this.notificacion = notificacion;
        this.creadaEn = creadaEn;
    }

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDni() {
        return dni;
    }

    public String getNombres() {
        return nombres;
    }

    public String getEmail() {
        return email;
    }

    public String getOficinaId() {
        return oficinaId;
    }

    public UUID getCupoId() {
        return cupoId;
    }

    public String getTipoTramite() {
        return tipoTramite;
    }

    public String getEstado() {
        return estado;
    }

    public String getNotificacion() {
        return notificacion;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }
}

