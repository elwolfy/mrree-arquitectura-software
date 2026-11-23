package pe.utec.consular.pagos.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code pagos.pago}.
 */
@Entity
@Table(name = "pago", schema = "pagos")
public class PagoJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tramite_id", nullable = false, updatable = false)
    private UUID tramiteId;

    @Column(nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal monto;

    @Column(nullable = false, length = 15)
    private String estado;

    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 64, updatable = false)
    private String claveIdempotencia;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    public PagoJpaEntity() {
    }

    public PagoJpaEntity(UUID id,
                         UUID tramiteId,
                         BigDecimal monto,
                         String estado,
                         String claveIdempotencia,
                         Instant creadoEn) {
        this.id = id;
        this.tramiteId = tramiteId;
        this.monto = monto;
        this.estado = estado;
        this.claveIdempotencia = claveIdempotencia;
        this.creadoEn = creadoEn;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTramiteId() {
        return tramiteId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getEstado() {
        return estado;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}

