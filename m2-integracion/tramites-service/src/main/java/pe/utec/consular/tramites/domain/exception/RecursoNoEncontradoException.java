package pe.utec.consular.tramites.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un trámite consular por su identificador.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

