package pe.utec.consular.monolito.compartido.domain.exception;

/**
 * Lanzada cuando el cupo seleccionado no cuenta con capacidad libre.
 * Mapeada al código RFC 9457: "cupo-sin-capacidad" (HTTP 409 Conflict).
 */
public class CupoSinCapacidadException extends ReglaNegocioException {
    public CupoSinCapacidadException(String mensaje) {
        super(mensaje);
    }
}
