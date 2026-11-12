package pe.utec.consular.citas.domain.exception;

/**
 * Excepción lanzada cuando la base de datos no está disponible o expira el timeout de conexión (modo CP).
 */
public class BaseDeDatosNoDisponibleException extends RuntimeException {

    public BaseDeDatosNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

