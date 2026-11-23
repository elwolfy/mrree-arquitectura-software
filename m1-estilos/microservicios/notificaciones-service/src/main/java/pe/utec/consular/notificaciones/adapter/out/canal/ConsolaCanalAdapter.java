package pe.utec.consular.notificaciones.adapter.out.canal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.utec.consular.notificaciones.application.port.out.CanalDeEnvioPort;
import pe.utec.consular.notificaciones.domain.Aviso;

/**
 * Adaptador de salida para envío de notificaciones mediante consola (logs).
 * <p>Concepto: Patrón Adapter (sección 2.1 del plan).
 * <p>Sesión: S1 y S2.
 */
@Component
@ConditionalOnProperty(name = "notificaciones.canal", havingValue = "CONSOLA", matchIfMissing = true)
public class ConsolaCanalAdapter implements CanalDeEnvioPort {

    private static final Logger log = LoggerFactory.getLogger(ConsolaCanalAdapter.class);

    @Override
    public void enviar(Aviso aviso) {
        log.info("[CANAL CONSOLA] Referencia: {} | Asunto: {} | Mensaje: {}",
                aviso.getReferencia(), aviso.getAsunto(), aviso.getCuerpo());
    }
}

