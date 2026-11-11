package pe.utec.consular.clean.citas.entities.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado (cita, oficina o cupo).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}

