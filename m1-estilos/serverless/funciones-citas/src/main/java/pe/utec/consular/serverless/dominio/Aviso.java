package pe.utec.consular.serverless.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Aviso(
        UUID id,
        String destinatario,
        String asunto,
        String cuerpo,
        CanalNotificacion canal,
        EstadoNotificacion estado,
        Instant creadoEn,
        String referencia
) {
    public Aviso {
        Objects.requireNonNull(id, "El ID del aviso no puede ser nulo");
        Objects.requireNonNull(destinatario, "El destinatario no puede ser nulo");
        Objects.requireNonNull(asunto, "El asunto no puede ser nulo");
        Objects.requireNonNull(cuerpo, "El cuerpo no puede ser nulo");
        Objects.requireNonNull(canal, "El canal no puede ser nulo");
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
        Objects.requireNonNull(creadoEn, "La fecha de creación no puede ser nula");
    }
}

