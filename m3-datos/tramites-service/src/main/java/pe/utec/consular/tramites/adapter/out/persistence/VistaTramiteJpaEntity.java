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
 * Entidad JPA para la tabla de proyección de lectura CQRS {@code tramites.vista_tramite}.
 */
@Entity
@Table(name = "vista_tramite", schema = "tramites")
public class VistaTramiteJpaEntity {

    @Id
    @Column(name = "tramite_id", nullable = false)
    private UUID tramiteId;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 8)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String dni;

    @Column(name = "tipo_tramite", nullable = false, length = 25)
    private String tipoTramite;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "motivo_rechazo", length = 250)
    private String motivoRechazo;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 30)
    private String subtipo;

    @Column(nullable = false, length = 2)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String pais;

    @Column(nullable = false)
    private int folios;

    @Column(nullable = false, length = 10)
    private String idioma;

    @Column(name = "pago_id")
    private UUID pagoId;

    @Column(name = "numero_documento", length = 40)
    private String numeroDocumento;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public VistaTramiteJpaEntity() {
    }

    public VistaTramiteJpaEntity(UUID tramiteId,
                                 String codigo,
                                 String dni,
                                 String tipoTramite,
                                 String estado,
                                 BigDecimal monto,
                                 String motivoRechazo,
                                 Instant actualizadoEn,
                                 String nombres,
                                 String email,
                                 String subtipo,
                                 String pais,
                                 int folios,
                                 String idioma,
                                 UUID pagoId,
                                 String numeroDocumento,
                                 Instant creadoEn) {
        this.tramiteId = tramiteId;
        this.codigo = codigo;
        this.dni = dni;
        this.tipoTramite = tipoTramite;
        this.estado = estado;
        this.monto = monto;
        this.motivoRechazo = motivoRechazo;
        this.actualizadoEn = actualizadoEn;
        this.nombres = nombres;
        this.email = email;
        this.subtipo = subtipo;
        this.pais = pais;
        this.folios = folios;
        this.idioma = idioma;
        this.pagoId = pagoId;
        this.numeroDocumento = numeroDocumento;
        this.creadoEn = creadoEn;
    }

    public UUID getTramiteId() {
        return tramiteId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDni() {
        return dni;
    }

    public String getTipoTramite() {
        return tipoTramite;
    }

    public String getEstado() {
        return estado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public String getNombres() {
        return nombres;
    }

    public String getEmail() {
        return email;
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

    public UUID getPagoId() {
        return pagoId;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}

