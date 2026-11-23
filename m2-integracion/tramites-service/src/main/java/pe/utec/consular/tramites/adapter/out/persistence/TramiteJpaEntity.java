package pe.utec.consular.tramites.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code tramites.tramite}.
 */
@Entity
@Table(name = "tramite", schema = "tramites")
public class TramiteJpaEntity {

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

    @Column(name = "tipo_tramite", nullable = false, length = 25)
    private String tipoTramite;

    @Column(nullable = false, length = 30)
    private String subtipo;

    @Column(nullable = false, length = 2)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String pais;

    @Column(nullable = false)
    private int folios;

    @Column(nullable = false, length = 10)
    private String idioma;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public TramiteJpaEntity() {
    }

    public TramiteJpaEntity(UUID id,
                            String codigo,
                            String dni,
                            String nombres,
                            String email,
                            String tipoTramite,
                            String subtipo,
                            String pais,
                            int folios,
                            String idioma,
                            String estado,
                            BigDecimal monto,
                            Instant creadoEn) {
        this.id = id;
        this.codigo = codigo;
        this.dni = dni;
        this.nombres = nombres;
        this.email = email;
        this.tipoTramite = tipoTramite;
        this.subtipo = subtipo;
        this.pais = pais;
        this.folios = folios;
        this.idioma = idioma;
        this.estado = estado;
        this.monto = monto;
        this.creadoEn = creadoEn;
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

    public String getTipoTramite() {
        return tipoTramite;
    }

    public String getSubtipo() {
        return subtipo;
    }

    public String getPais() {
        return pais;
    }

    public int getFolios() {
        return folios;
    }

    public String getIdioma() {
        return idioma;
    }

    public String getEstado() {
        return estado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}

