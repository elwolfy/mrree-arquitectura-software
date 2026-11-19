package pe.utec.consular.pagos.domain.exception;

/**
 * Excepción lanzada cuando una orden de pago solicitada no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

