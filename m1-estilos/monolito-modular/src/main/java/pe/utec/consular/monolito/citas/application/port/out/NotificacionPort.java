package pe.utec.consular.monolito.citas.application.port.out;

import pe.utec.consular.monolito.citas.domain.Cita;

/**
 * Puerto de salida para despachar notificaciones asociadas al dominio de citas.
 * <p>Concepto: Arquitectura hexagonal y acoplamiento deliberado (secciones 1.2 y 1.3).
 * <p>Sesión: S1 y S2. En el monolito modular, este puerto es implementado por un adaptador
 * que invoca síncronamente la API pública del módulo notificaciones.
 */
public interface NotificacionPort {

    /**
     * Notifica al ciudadano sobre la reserva confirmada de su cita.
     *
     * @param cita datos de la cita reservada
     */
    void notificarReserva(Cita cita);
}
