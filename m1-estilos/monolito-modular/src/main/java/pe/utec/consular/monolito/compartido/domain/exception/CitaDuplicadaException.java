package pe.utec.consular.monolito.compartido.domain.exception;

/**
 * Lanzada cuando un ciudadano ya cuenta con una cita activa para el mismo día y oficina.
 * Mapeada al código RFC 9457: "cita-duplicada" (HTTP 409 Conflict).
 */
public class CitaDuplicadaException extends ReglaNegocioException {
    public CitaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
