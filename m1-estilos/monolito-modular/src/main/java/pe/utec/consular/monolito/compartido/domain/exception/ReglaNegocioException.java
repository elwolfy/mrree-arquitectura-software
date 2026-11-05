package pe.utec.consular.monolito.compartido.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio en el dominio consular.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
