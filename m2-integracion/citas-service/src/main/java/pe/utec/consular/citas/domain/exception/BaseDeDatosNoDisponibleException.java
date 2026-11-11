package pe.utec.consular.citas.domain.exception;

/**
 * Excepción lanzada cuando la base de datos no está disponible o excede el tiempo de espera.
 */
public class BaseDeDatosNoDisponibleException extends RuntimeException {

    public BaseDeDatosNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}

