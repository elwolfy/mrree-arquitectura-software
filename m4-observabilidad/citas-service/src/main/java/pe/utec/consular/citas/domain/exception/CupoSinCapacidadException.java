package pe.utec.consular.citas.domain.exception;

/**
 * Excepción lanzada cuando se intenta reservar un cupo cuya capacidad libre se ha agotado.
 */
public class CupoSinCapacidadException extends ReglaNegocioException {

    public CupoSinCapacidadException(String message) {
        super(message);
    }
}

