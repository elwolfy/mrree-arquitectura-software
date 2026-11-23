package pe.utec.consular.tramites.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entidad JPA para la tabla inmutable de Event Sourcing {@code tramites.eventos_tramite}.
 */
@Entity
@Table(name = "eventos_tramite", schema = "tramites")
public class EventoTramiteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tramite_id", nullable = false, updatable = false)
    private UUID tramiteId;

    @Column(nullable = false, updatable = false)
    private int version;

    @Column(nullable = false, length = 40, updatable = false)
    private String tipo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, updatable = false)
    private Map<String, Object> datos;

    @Column(name = "ocurrido_en", nullable = false, updatable = false)
    private Instant ocurridoEn;

    public EventoTramiteJpaEntity() {
    }

    public EventoTramiteJpaEntity(UUID tramiteId,
                                  int version,
                                  String tipo,
                                  Map<String, Object> datos,
                                  Instant ocurridoEn) {
        this.tramiteId = tramiteId;
        this.version = version;
        this.tipo = tipo;
        this.datos = datos;
        this.ocurridoEn = ocurridoEn;
    }

    public Long getId() {
        return id;
    }

    public UUID getTramiteId() {
        return tramiteId;
    }

    public int getVersion() {
        return version;
    }

    public String getTipo() {
        return tipo;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }

    public Instant getOcurridoEn() {
        return ocurridoEn;
    }
}

