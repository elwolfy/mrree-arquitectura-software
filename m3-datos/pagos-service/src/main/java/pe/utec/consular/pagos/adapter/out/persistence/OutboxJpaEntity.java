package pe.utec.consular.pagos.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code pagos.outbox} (Transactional Outbox propio de pagos-service).
 */
@Entity
@Table(name = "outbox", schema = "pagos")
public class OutboxJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "agregado_id", nullable = false, updatable = false)
    private UUID agregadoId;

    @Column(nullable = false, length = 60, updatable = false)
    private String tipo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, updatable = false)
    private Map<String, Object> carga;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "publicado_en")
    private Instant publicadoEn;

    public OutboxJpaEntity() {
    }

    public OutboxJpaEntity(UUID id,
                           UUID agregadoId,
                           String tipo,
                           Map<String, Object> carga,
                           Instant creadoEn,
                           Instant publicadoEn) {
        this.id = id;
        this.agregadoId = agregadoId;
        this.tipo = tipo;
        this.carga = carga;
        this.creadoEn = creadoEn;
        this.publicadoEn = publicadoEn;
    }

    public void marcarPublicado(Instant momento) {
        this.publicadoEn = momento;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAgregadoId() {
        return agregadoId;
    }

    public String getTipo() {
        return tipo;
    }

    public Map<String, Object> getCarga() {
        return carga;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getPublicadoEn() {
        return publicadoEn;
    }
}

