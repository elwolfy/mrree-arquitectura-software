package pe.utec.consular.emision.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidad JPA para la tabla de idempotencia {@code emision.mensaje_procesado}.
 */
@Entity
@Table(name = "mensaje_procesado", schema = "emision")
public class MensajeProcesadoJpaEntity {

    @Id
    @Column(name = "message_id", length = 100, nullable = false)
    private String messageId;

    @Column(name = "procesado_en", nullable = false)
    private Instant procesadoEn;

    public MensajeProcesadoJpaEntity() {
    }

    public MensajeProcesadoJpaEntity(String messageId, Instant procesadoEn) {
        this.messageId = messageId;
        this.procesadoEn = procesadoEn;
    }

    public String getMessageId() {
        return messageId;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }
}

