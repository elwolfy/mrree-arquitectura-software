package pe.utec.consular.clean.citas.entities.exception;

/**
 * Excepción lanzada cuando un cupo horario alcanzó su capacidad máxima.
 */
public class CupoSinCapacidadException extends RuntimeException {

    public CupoSinCapacidadException(String message) {
        super(message);
    }
}

