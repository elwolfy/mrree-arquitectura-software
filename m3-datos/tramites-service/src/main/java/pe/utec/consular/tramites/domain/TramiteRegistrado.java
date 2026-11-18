package pe.utec.consular.tramites.domain;

import java.util.UUID;

/**
 * Carga útil del evento de integración {@code tramite.registrado} publicado en RabbitMQ
 * hacia el exchange {@code consular.eventos} (sección 6.1 del contrato global).
 */
public record TramiteRegistrado(
        UUID tramiteId,
        String codigo,
        String dni,
        String email,
        TipoTramite tipoTramite
) {
}

