package pe.utec.consular.notificaciones.adapter.in.messaging;

import java.util.UUID;

/**
 * Carga útil publicada por {@code tramites-service} con routing key {@code tramite.observado}
 * (Andamiaje de la Asignación A2).
 * <p>Incluye los métodos de plantilla para el asunto y cuerpo del aviso
 * ("Su trámite requiere una corrección").
 */
public record TramiteObservadoEvent(
        UUID tramiteId,
        String codigo,
        String dni,
        String email,
        String motivo
) {
    public String asuntoAviso() {
        return "Su trámite requiere una corrección - " + codigo;
    }

    public String cuerpoAviso() {
        return String.format(
                "Estimado(a) ciudadano(a) con DNI %s,%n%nSu trámite requiere una corrección (%s): %s.",
                dni, codigo, motivo
        );
    }
}

