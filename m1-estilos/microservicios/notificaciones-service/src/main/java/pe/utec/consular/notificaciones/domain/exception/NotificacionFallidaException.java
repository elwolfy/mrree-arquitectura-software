package pe.utec.consular.notificaciones.domain.exception;

/**
 * Excepción lanzada cuando ocurre una falla en el envío de la notificación (real o simulada).
 * <p>Concepto: Simulación de fallas en M1 (sección 4.3 del contrato global).
 */
public class NotificacionFallidaException extends RuntimeException {

    public NotificacionFallidaException(String mensaje) {
        super(mensaje);
    }

    public NotificacionFallidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

