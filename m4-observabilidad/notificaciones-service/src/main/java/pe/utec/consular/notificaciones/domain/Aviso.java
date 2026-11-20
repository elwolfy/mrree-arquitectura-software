package pe.utec.consular.notificaciones.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un aviso enviado a un ciudadano.
 */
public record Aviso(
        UUID id,
        String dni,
        String destinatario,
        String asunto,
        String cuerpo,
        String canal,
        String estado,
        Instant creadoEn,
        String referencia
) {
    public Aviso {
        Objects.requireNonNull(id, "El id no puede ser nulo");
        Objects.requireNonNull(dni, "El DNI no puede ser nulo");
        Objects.requireNonNull(destinatario, "El destinatario no puede ser nulo");
        Objects.requireNonNull(asunto, "El asunto no puede ser nulo");
        Objects.requireNonNull(cuerpo, "El cuerpo no puede ser nulo");
        Objects.requireNonNull(canal, "El canal no puede ser nulo");
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
        Objects.requireNonNull(creadoEn, "La fecha de creación no puede ser nula");
        Objects.requireNonNull(referencia, "La referencia no puede ser nula");
    }

    public static Aviso nuevoEnviado(String dni, String destinatario, String asunto, String cuerpo, String referencia) {
        return new Aviso(
                UUID.randomUUID(),
                dni,
                destinatario,
                asunto,
                cuerpo,
                "CORREO",
                "ENVIADA",
                Instant.now(),
                referencia
        );
    }
}

