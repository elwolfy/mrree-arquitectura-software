package pe.utec.consular.clean.citas.interfaceadapters.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.utec.consular.clean.citas.entities.Cita;
import pe.utec.consular.clean.citas.entities.exception.NotificacionFallidaException;
import pe.utec.consular.clean.citas.usecase.NotificadorGateway;

import java.util.function.BooleanSupplier;

/**
 * Gateway de notificación por consola para la comparativa Clean Architecture.
 * Soporta la simulación pedagógica de falla síncrona ({@code notificaciones.simular-falla=true})
 * para demostrar el rollback transaccional del monolito.
 * <p>Concepto: Clean Architecture – Interface Adapter Gateway (Círculo 3, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code NotificacionModuloAdapter} y {@code ConsolaCanalAdapter} en {@code monolito-modular}.
 */
public class NotificadorConsolaGateway implements NotificadorGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificadorConsolaGateway.class);

    private final BooleanSupplier simularFallaSupplier;

    public NotificadorConsolaGateway(BooleanSupplier simularFallaSupplier) {
        this.simularFallaSupplier = simularFallaSupplier;
    }

    @Override
    public void notificarReserva(Cita cita) {
        if (simularFallaSupplier.getAsBoolean()) {
            log.error("[SIMULACIÓN] Falla forzada en NotificadorConsolaGateway para cita {}", cita.getCodigo().valor());
            throw new NotificacionFallidaException(
                    "Falla simulada en el servicio de notificaciones (notificaciones.simular-falla=true)");
        }

        log.info("[NOTIFICACIÓN CONSOLA - CLEAN] Cita {} reservada para DNI {} en oficina {} ({} {})",
                cita.getCodigo().valor(),
                cita.getDni().enmascarado(),
                cita.getOficinaId(),
                cita.getFecha(),
                cita.getHora());
    }
}

