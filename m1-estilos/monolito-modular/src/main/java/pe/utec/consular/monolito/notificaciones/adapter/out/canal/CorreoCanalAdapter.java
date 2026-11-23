package pe.utec.consular.monolito.notificaciones.adapter.out.canal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.notificaciones.application.port.out.CanalDeEnvioPort;
import pe.utec.consular.monolito.notificaciones.domain.Aviso;

/**
 * Adaptador de salida para remisión de correos vía SMTP (Mailpit).
 * <p>Concepto: Patrón Adapter (sección 2.1 del plan).
 * <p>Sesión: S2. Demuestra la sustitución del canal de salida mediante configuración sin modificar lógica de negocio.
 */
@Component
@ConditionalOnProperty(name = "notificaciones.canal", havingValue = "CORREO")
public class CorreoCanalAdapter implements CanalDeEnvioPort {

    private static final Logger log = LoggerFactory.getLogger(CorreoCanalAdapter.class);

    private final JavaMailSender mailSender;

    public CorreoCanalAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviar(Aviso aviso) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("noreply@consular.gob.pe");
            mensaje.setTo(aviso.getDestinatario());
            mensaje.setSubject(aviso.getAsunto());
            mensaje.setText(aviso.getCuerpo());

            mailSender.send(mensaje);
            log.info("[CANAL CORREO] Correo electrónico enviado a {} mediante servidor SMTP Mailpit",
                    aviso.getDestinatario());
        } catch (Exception ex) {
            log.error("Error al despachar correo a {}: {}", aviso.getDestinatario(), ex.getMessage(), ex);
            throw new RuntimeException("Error en canal de correo SMTP", ex);
        }
    }
}
