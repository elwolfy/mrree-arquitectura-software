package pe.utec.consular.monolito.compartido.domain.exception;

/**
 * Lanzada cuando el canal de notificaciones falla o se activa la simulación de falla.
 * En el monolito, al invocarse de forma síncrona dentro de la transacción, revierte toda la reserva.
 * <p>Concepto: Acoplamiento deliberado en arquitectura monolítica (sección 1.2 del plan).
 */
public class NotificacionFallidaException extends ReglaNegocioException {
    public NotificacionFallidaException(String mensaje) {
        super(mensaje);
    }

    public NotificacionFallidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
