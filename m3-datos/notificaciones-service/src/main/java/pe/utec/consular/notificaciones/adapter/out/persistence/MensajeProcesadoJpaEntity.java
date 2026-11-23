package pe.utec.consular.notificaciones.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la tabla de idempotencia {@code notificaciones.mensaje_procesado}.
 */
@Entity
@Table(name = "mensaje_procesado", schema = "notificaciones")
public class MensajeProcesadoJpaEntity {

    @Id
    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "tipo_evento", nullable = false, length = 60)
    private String tipoEvento;

    @Column(name = "procesado_en", nullable = false)
    private Instant procesadoEn;

    public MensajeProcesadoJpaEntity() {
    }

    public MensajeProcesadoJpaEntity(UUID messageId, String tipoEvento, Instant procesadoEn) {
        this.messageId = messageId;
        this.tipoEvento = tipoEvento;
        this.procesadoEn = procesadoEn;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }
}

