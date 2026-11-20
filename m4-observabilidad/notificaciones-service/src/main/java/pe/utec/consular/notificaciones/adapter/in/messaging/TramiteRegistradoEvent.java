package pe.utec.consular.notificaciones.adapter.in.messaging;

import java.util.UUID;

/**
 * Carga útil publicada por {@code tramites-service} con routing key {@code tramite.registrado}
 * (relación Conformista frente al contrato de eventos de la sección 6.1).
 */
public record TramiteRegistradoEvent(
        UUID tramiteId,
        String codigo,
        String dni,
        String email,
        String tipoTramite
) {
}

