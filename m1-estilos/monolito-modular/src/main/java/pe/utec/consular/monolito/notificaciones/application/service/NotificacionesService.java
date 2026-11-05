package pe.utec.consular.monolito.notificaciones.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.monolito.compartido.domain.exception.NotificacionFallidaException;
import pe.utec.consular.monolito.compartido.infraestructura.NotificacionesProperties;
import pe.utec.consular.monolito.notificaciones.AvisoCommand;
import pe.utec.consular.monolito.notificaciones.NotificacionesApi;
import pe.utec.consular.monolito.notificaciones.application.port.out.AvisoRepositoryPort;
import pe.utec.consular.monolito.notificaciones.application.port.out.CanalDeEnvioPort;
import pe.utec.consular.monolito.notificaciones.domain.Aviso;
import pe.utec.consular.monolito.notificaciones.domain.CanalNotificacion;

/**
 * Servicio de aplicación que implementa la API pública del módulo de notificaciones.
 * <p>Concepto: Acoplamiento deliberado para la comparación de estilos (1.2 del plan).
 * En el monolito se invoca de forma síncrona dentro de la misma transacción de la reserva.
 * <p>Sesión: S1 y S2.
 */
@Service
public class NotificacionesService implements NotificacionesApi {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesService.class);

    private final CanalDeEnvioPort canalDeEnvioPort;
    private final AvisoRepositoryPort avisoRepositoryPort;
    private final NotificacionesProperties notificacionesProperties;

    public NotificacionesService(CanalDeEnvioPort canalDeEnvioPort,
                                 AvisoRepositoryPort avisoRepositoryPort,
                                 NotificacionesProperties notificacionesProperties) {
        this.canalDeEnvioPort = canalDeEnvioPort;
        this.avisoRepositoryPort = avisoRepositoryPort;
        this.notificacionesProperties = notificacionesProperties;
    }

    @Override
    @Transactional
    public void enviar(AvisoCommand comando) {
        log.info("Procesando solicitud de notificación para destinatario {} con referencia {}",
                comando.destinatario(), comando.referencia());

        // Simulación de falla según configuración pedagógica
        if (notificacionesProperties.isSimularFalla()) {
            log.warn("Simulación de falla activa en notificaciones (notificaciones.simular-falla=true)");
            throw new NotificacionFallidaException("Falla simulada en el envío de notificaciones");
        }

        CanalNotificacion canal = "CORREO".equalsIgnoreCase(notificacionesProperties.getCanal())
                ? CanalNotificacion.CORREO
                : CanalNotificacion.CONSOLA;

        Aviso aviso = Aviso.nuevo(
                comando.destinatario(),
                comando.asunto(),
                comando.cuerpo(),
                canal,
                comando.referencia()
        );

        canalDeEnvioPort.enviar(aviso);
        avisoRepositoryPort.guardar(aviso);

        log.info("Aviso {} guardado y enviado satisfactoriamente por canal {}", aviso.getId(), canal);
    }
}
