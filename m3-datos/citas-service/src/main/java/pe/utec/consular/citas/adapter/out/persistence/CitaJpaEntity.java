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
 * Entidad JPA para la tabla citas.cita.
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

    @ManyToOne(fetch = FetchType.EAGER)
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

    public CitaJpaEntity(UUID id, String codigo, String dni, String nombres, String email, String oficinaId,
                         CupoJpaEntity cupo, String tipoTramite,
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

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOficinaId() {
        return oficinaId;
    }

    public void setOficinaId(String oficinaId) {
        this.oficinaId = oficinaId;
    }

    public CupoJpaEntity getCupo() {
        return cupo;
    }

    public void setCupo(CupoJpaEntity cupo) {
        this.cupo = cupo;
    }

    public UUID getCupoId() {
        return cupo != null ? cupo.getId() : null;
    }

    public LocalDate getFecha() {
        return cupo != null ? cupo.getFecha() : null;
    }

    public LocalTime getHora() {
        return cupo != null ? cupo.getHora() : null;
    }

    public String getTipoTramite() {
        return tipoTramite;
    }

    public void setTipoTramite(String tipoTramite) {
        this.tipoTramite = tipoTramite;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotificacion() {
        return notificacion;
    }

    public void setNotificacion(String notificacion) {
        this.notificacion = notificacion;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(Instant creadaEn) {
        this.creadaEn = creadaEn;
    }
}

