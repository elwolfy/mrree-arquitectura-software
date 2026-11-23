package pe.utec.consular.citas.adapter.out.http;

/**
 * Cuerpo JSON enviado por {@code citas-service} hacia {@code POST /api/avisos} de {@code notificaciones-service}.
 */
public record AvisoHttpRequest(
        String destinatario,
        String asunto,
        String cuerpo,
        String referencia
) {
}

