package pe.utec.consular.citas.application.port.out;

import pe.utec.consular.citas.domain.Cita;
import pe.utec.consular.citas.domain.EstadoNotificacion;

/**
 * Puerto de salida para despachar notificaciones asociadas al dominio de citas.
 * <p>Concepto: Arquitectura hexagonal y resiliencia entre microservicios (secciones 1.2, 1.3 y 4.3).
 * <p>Sesión: S1. En la variante de microservicios, este puerto es implementado por
 * {@code NotificacionHttpAdapter} (RestClient + Circuit Breaker Resilience4j). Si el servicio de
 * notificaciones responde satisfactoriamente, devuelve {@link EstadoNotificacion#ENVIADA}; si falla
 * o el circuito está abierto, el fallback devuelve {@link EstadoNotificacion#PENDIENTE} sin abortar
 * la reserva.
 */
public interface NotificacionPort {

    EstadoNotificacion notificarReserva(Cita cita);
}

