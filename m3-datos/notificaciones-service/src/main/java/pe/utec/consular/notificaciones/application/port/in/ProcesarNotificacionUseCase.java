package pe.utec.consular.notificaciones.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada para procesar de manera idempotente el envío de un aviso originado por un evento.
 */
public interface ProcesarNotificacionUseCase {

    boolean procesarEvento(UUID messageId,
                           String tipoEvento,
                           String dni,
                           String destinatario,
                           String asunto,
                           String cuerpo,
                           String referencia);
}

