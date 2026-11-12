package pe.utec.consular.citas.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado (oficina, cupo o cita).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

