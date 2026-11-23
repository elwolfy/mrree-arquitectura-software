package pe.utec.consular.notificaciones.application.port.out;

/**
 * Puerto de salida para el envío de correos electrónicos a través de SMTP (Mailpit).
 */
public interface CorreoSenderPort {

    void enviarCorreo(String destinatario, String asunto, String cuerpo);
}

