package pe.utec.consular.monolito.compartido.domain.exception;

/**
 * Lanzada cuando no se localiza una entidad requerida (oficina, cita, cupo).
 * Mapeada al código RFC 9457: "no-encontrado" (HTTP 404 Not Found).
 */
public class RecursoNoEncontradoException extends ReglaNegocioException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
