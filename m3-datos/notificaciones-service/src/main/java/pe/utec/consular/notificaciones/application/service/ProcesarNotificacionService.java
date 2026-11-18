package pe.utec.consular.notificaciones.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.notificaciones.application.port.in.ProcesarNotificacionUseCase;
import pe.utec.consular.notificaciones.application.port.out.AvisoRepositoryPort;
import pe.utec.consular.notificaciones.application.port.out.CorreoSenderPort;
import pe.utec.consular.notificaciones.application.port.out.MensajeProcesadoPort;
import pe.utec.consular.notificaciones.domain.Aviso;

import java.util.UUID;

/**
 * Servicio de aplicación que garantiza el procesamiento idempotente y transaccional
 * de los eventos recibidos desde RabbitMQ.
 */
@Service
public class ProcesarNotificacionService implements ProcesarNotificacionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcesarNotificacionService.class);

    private final AvisoRepositoryPort avisoRepositoryPort;
    private final MensajeProcesadoPort mensajeProcesadoPort;
    private final CorreoSenderPort correoSenderPort;
    private volatile boolean fallarSiempre;

    public ProcesarNotificacionService(
            AvisoRepositoryPort avisoRepositoryPort,
            MensajeProcesadoPort mensajeProcesadoPort,
            CorreoSenderPort correoSenderPort,
            @Value("${notificaciones.fallar-siempre:false}") boolean fallarSiempre) {
        this.avisoRepositoryPort = avisoRepositoryPort;
        this.mensajeProcesadoPort = mensajeProcesadoPort;
        this.correoSenderPort = correoSenderPort;
        this.fallarSiempre = fallarSiempre;
    }

    public void setFallarSiempre(boolean fallarSiempre) {
        this.fallarSiempre = fallarSiempre;
    }

    @Override
    @Transactional
    public boolean procesarEvento(UUID messageId,
                                  String tipoEvento,
                                  String dni,
                                  String destinatario,
                                  String asunto,
                                  String cuerpo,
                                  String referencia) {
        if (fallarSiempre) {
            log.error("Simulación didáctica activa (NOTIFICACIONES_FALLAR_SIEMPRE=true) al procesar messageId={}", messageId);
            throw new IllegalStateException("Falla simulada en notificaciones-service para demostrar reintentos y DLQ");
        }

        if (mensajeProcesadoPort.yaFueProcesado(messageId)) {
            log.info("Mensaje duplicado detectado y descartado por idempotencia: messageId={}, tipoEvento={}",
                    messageId, tipoEvento);
            return false;
        }

        // 1. Registrar el messageId en mensaje_procesado dentro de la misma transacción
        mensajeProcesadoPort.registrarProcesado(messageId, tipoEvento);

        // 2. Persistir el aviso en la tabla notificaciones.aviso
        Aviso aviso = Aviso.nuevoEnviado(dni, destinatario, asunto, cuerpo, referencia);
        avisoRepositoryPort.guardar(aviso);

        // 3. Enviar el correo electrónico a Mailpit
        correoSenderPort.enviarCorreo(destinatario, asunto, cuerpo);

        log.info("Aviso procesado y correo enviado para referencia {} (messageId={})", referencia, messageId);
        return true;
    }
}

