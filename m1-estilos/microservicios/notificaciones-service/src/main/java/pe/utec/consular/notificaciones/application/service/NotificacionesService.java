package pe.utec.consular.notificaciones.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.notificaciones.application.port.in.ConsultarAvisosUseCase;
import pe.utec.consular.notificaciones.application.port.in.EnviarAvisoCommand;
import pe.utec.consular.notificaciones.application.port.in.EnviarAvisoUseCase;
import pe.utec.consular.notificaciones.application.port.out.AvisoRepositoryPort;
import pe.utec.consular.notificaciones.application.port.out.CanalDeEnvioPort;
import pe.utec.consular.notificaciones.application.port.out.ConfiguracionNotificacionesPort;
import pe.utec.consular.notificaciones.domain.Aviso;
import pe.utec.consular.notificaciones.domain.CanalNotificacion;
import pe.utec.consular.notificaciones.domain.exception.NotificacionFallidaException;

import java.util.List;

/**
 * Servicio de aplicación para envío y consulta de avisos en {@code notificaciones-service}.
 * <p>Concepto: Arquitectura hexagonal – servicio de aplicación y simulación de fallas (secciones 1.2, 1.3 y 4.3).
 * <p>Sesión: S1 y S2.
 */
@Service
public class NotificacionesService implements EnviarAvisoUseCase, ConsultarAvisosUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesService.class);

    private final CanalDeEnvioPort canalDeEnvioPort;
    private final AvisoRepositoryPort avisoRepositoryPort;
    private final ConfiguracionNotificacionesPort configuracionPort;

    public NotificacionesService(CanalDeEnvioPort canalDeEnvioPort,
                                 AvisoRepositoryPort avisoRepositoryPort,
                                 ConfiguracionNotificacionesPort configuracionPort) {
        this.canalDeEnvioPort = canalDeEnvioPort;
        this.avisoRepositoryPort = avisoRepositoryPort;
        this.configuracionPort = configuracionPort;
    }

    @Override
    @Transactional
    public Aviso enviar(EnviarAvisoCommand comando) {
        log.info("Procesando solicitud de aviso con referencia {}", comando.referencia());

        if (configuracionPort.isSimularFalla()) {
            log.warn("Simulación de falla activa en notificaciones-service (notificaciones.simular-falla=true)");
            throw new NotificacionFallidaException("Falla simulada en el servicio de notificaciones");
        }

        CanalNotificacion canal = "CORREO".equalsIgnoreCase(configuracionPort.getCanal())
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
        Aviso guardado = avisoRepositoryPort.guardar(aviso);

        log.info("Aviso {} guardado y enviado satisfactoriamente por canal {}", guardado.getId(), canal);
        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aviso> consultarPorReferencia(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return avisoRepositoryPort.listarTodos();
        }
        return avisoRepositoryPort.buscarPorReferencia(referencia);
    }
}

