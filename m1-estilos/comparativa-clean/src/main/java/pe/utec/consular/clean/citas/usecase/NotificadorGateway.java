package pe.utec.consular.clean.citas.usecase;

import pe.utec.consular.clean.citas.entities.Cita;

/**
 * Interfaz de notificación requerida por el caso de uso de reserva de citas.
 * <p>Concepto: Clean Architecture – Círculo 2 ({@code usecase}, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code NotificacionPort} en {@code monolito-modular}.
 */
public interface NotificadorGateway {

    void notificarReserva(Cita cita);
}

