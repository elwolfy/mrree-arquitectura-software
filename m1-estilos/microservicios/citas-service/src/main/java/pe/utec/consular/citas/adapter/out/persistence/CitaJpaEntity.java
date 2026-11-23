package pe.utec.consular.citas.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code citas.cita}.
 */
@Entity
@Table(name = "cita", schema = "citas")
public class CitaJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 8)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String dni;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "oficina_id", nullable = false, length = 5)
    private String oficinaId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cupo_id", nullable = false)
    private CupoJpaEntity cupo;

    @Column(name = "tipo_tramite", nullable = false, length = 20)
    private String tipoTramite;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false, length = 20)
    private String notificacion;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    public CitaJpaEntity() {
    }

    public CitaJpaEntity(UUID id, String codigo, String dni, String nombres, String email,
                         String oficinaId, CupoJpaEntity cupo, String tipoTramite,
                         String estado, String notificacion, Instant creadaEn) {
        this.id = id;
        this.codigo = codigo;
        this.dni = dni;
        this.nombres = nombres;
        this.email = email;
        this.oficinaId = oficinaId;
        this.cupo = cupo;
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
        return cupo.getId();
    }

    public LocalDate getFecha() {
        return cupo.getFecha();
    }

    public LocalTime getHora() {
        return cupo.getHora();
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

