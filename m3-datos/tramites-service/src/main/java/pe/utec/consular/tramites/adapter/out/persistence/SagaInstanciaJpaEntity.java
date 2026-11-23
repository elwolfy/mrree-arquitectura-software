package pe.utec.consular.tramites.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code tramites.saga_instancia}.
 */
@Entity
@Table(name = "saga_instancia", schema = "tramites")
public class SagaInstanciaJpaEntity {

    @Id
    @Column(name = "tramite_id", nullable = false)
    private UUID tramiteId;

    @Column(nullable = false, length = 40)
    private String paso;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public SagaInstanciaJpaEntity() {
    }

    public SagaInstanciaJpaEntity(UUID tramiteId, String paso, String estado, Instant actualizadoEn) {
        this.tramiteId = tramiteId;
        this.paso = paso;
        this.estado = estado;
        this.actualizadoEn = actualizadoEn;
    }

    public UUID getTramiteId() {
        return tramiteId;
    }

    public String getPaso() {
        return paso;
    }

    public String getEstado() {
        return estado;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}

