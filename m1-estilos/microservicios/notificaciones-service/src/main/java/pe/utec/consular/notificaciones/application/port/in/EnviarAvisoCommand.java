package pe.utec.consular.notificaciones.application.port.in;

/**
 * Comando de entrada para el caso de uso de envío de aviso.
 * <p>Concepto: Objeto de comando inmutable (Java 21 record) en puerto primario.
 */
public record EnviarAvisoCommand(
        String destinatario,
        String asunto,
        String cuerpo,
        String referencia
) {
}

