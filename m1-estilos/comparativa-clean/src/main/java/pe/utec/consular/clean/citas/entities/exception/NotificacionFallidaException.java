package pe.utec.consular.clean.citas.entities.exception;

/**
 * Excepción lanzada cuando falla el envío síncrono de una notificación en el monolito.
 */
public class NotificacionFallidaException extends RuntimeException {

    public NotificacionFallidaException(String message) {
        super(message);
    }

    public NotificacionFallidaException(String message, Throwable cause) {
        super(message, cause);
    }
}

