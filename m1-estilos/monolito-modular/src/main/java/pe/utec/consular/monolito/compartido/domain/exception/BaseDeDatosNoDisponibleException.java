package pe.utec.consular.monolito.compartido.domain.exception;

/**
 * Lanzada cuando la base de datos no responde dentro del tiempo límite o existe partición en modo CP.
 * Mapeada al código RFC 9457: "base-de-datos-no-disponible" (HTTP 503 Service Unavailable).
 * <p>Concepto: Teorema CAP (sección 1.4 del plan).
 */
public class BaseDeDatosNoDisponibleException extends ReglaNegocioException {
    public BaseDeDatosNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public BaseDeDatosNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
