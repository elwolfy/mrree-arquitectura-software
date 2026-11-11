package pe.utec.consular.clean.citas.entities.exception;

/**
 * Excepción lanzada cuando la base de datos no está disponible (modo CP).
 */
public class BaseDeDatosNoDisponibleException extends RuntimeException {

    public BaseDeDatosNoDisponibleException(String message) {
        super(message);
    }

    public BaseDeDatosNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}

