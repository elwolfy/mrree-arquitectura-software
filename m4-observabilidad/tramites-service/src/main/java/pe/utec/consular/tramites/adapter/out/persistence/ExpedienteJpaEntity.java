package pe.utec.consular.tramites.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

/**
 * Entidad JPA para la tabla documental {@code tramites.expediente} (columna {@code metadatos JSONB}).
 * <p>Concepto: Modelo de datos orientado a documentos en PostgreSQL (JSONB + índice GIN)
 * para atributos variables según el tipo de trámite (sección 6.3 del contrato global).
 * <p>Sesión: S5.
 */
@Entity
@Table(name = "expediente", schema = "tramites")
public class ExpedienteJpaEntity {

    @Id
    @Column(name = "tramite_id", nullable = false)
    private UUID tramiteId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadatos", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadatos;

    public ExpedienteJpaEntity() {
    }

    public ExpedienteJpaEntity(UUID tramiteId, Map<String, Object> metadatos) {
        this.tramiteId = tramiteId;
        this.metadatos = metadatos;
    }

    public UUID getTramiteId() {
        return tramiteId;
    }

    public Map<String, Object> getMetadatos() {
        return metadatos;
    }
}

