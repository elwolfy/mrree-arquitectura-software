package pe.utec.consular.emision.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para {@code emision.documento_emitido}.
 */
@Entity
@Table(name = "documento_emitido", schema = "emision")
public class DocumentoEmitidoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tramite_id", nullable = false, unique = true)
    private UUID tramiteId;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 30)
    private String numeroDocumento;

    @Column(name = "emitido_en", nullable = false)
    private Instant emitidoEn;

    public DocumentoEmitidoJpaEntity() {
    }

    public DocumentoEmitidoJpaEntity(UUID id, UUID tramiteId, String numeroDocumento, Instant emitidoEn) {
        this.id = id;
        this.tramiteId = tramiteId;
        this.numeroDocumento = numeroDocumento;
        this.emitidoEn = emitidoEn;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTramiteId() {
        return tramiteId;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public Instant getEmitidoEn() {
        return emitidoEn;
    }
}

