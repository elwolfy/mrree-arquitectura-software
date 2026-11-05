package pe.utec.consular.monolito.notificaciones;

/**
 * Comando para solicitar el envío de una notificación (aviso).
 * <p>Concepto: DTO / Comando de API pública de módulo en Spring Modulith.
 * <p>Sesión: S1.
 */
public record AvisoCommand(
        String destinatario,
        String asunto,
        String cuerpo,
        String referencia
) {
}
