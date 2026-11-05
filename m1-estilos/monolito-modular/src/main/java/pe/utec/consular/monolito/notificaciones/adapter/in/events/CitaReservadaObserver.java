package pe.utec.consular.monolito.notificaciones.adapter.in.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.compartido.domain.CitaReservadaInfo;

/**
 * Observador de eventos de dominio para citas reservadas.
 * <p>Concepto: Patrón Observer (sección 2.1 del plan).
 * <p>Sesión: S2. No reemplaza la llamada síncrona; demuestra desacoplamiento por eventos en Spring Modulith.
 */
@Component
public class CitaReservadaObserver {

    private static final Logger log = LoggerFactory.getLogger(CitaReservadaObserver.class);

    @ApplicationModuleListener
    public void alReservarCita(CitaReservadaInfo evento) {
        log.info("[OBSERVER] Evento CitaReservada recibido para cita {} y DNI {}. Patrón Observer didáctico.",
                evento.codigo(), evento.dni());
    }
}
