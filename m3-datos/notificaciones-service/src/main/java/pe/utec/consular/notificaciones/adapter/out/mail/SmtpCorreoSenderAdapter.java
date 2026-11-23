package pe.utec.consular.notificaciones.adapter.out.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pe.utec.consular.notificaciones.application.port.out.CorreoSenderPort;

/**
 * Adaptador SMTP que envía correos electrónicos hacia Mailpit.
 */
@Component
public class SmtpCorreoSenderAdapter implements CorreoSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpCorreoSenderAdapter.class);
    private static final String REMITENTE = "notificaciones@consular.lab";

    private final JavaMailSender mailSender;

    public SmtpCorreoSenderAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(REMITENTE);
        message.setTo(destinatario);
        message.setSubject(asunto);
        message.setText(cuerpo);
        mailSender.send(message);
        log.info("Correo SMTP enviado satisfactoriamente a {} con asunto '{}'", destinatario, asunto);
    }
}

