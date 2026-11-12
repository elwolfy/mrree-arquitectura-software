package pe.utec.consular.citas.domain.exception;

/**
 * Excepción lanzada cuando el cupo horario seleccionado ya no cuenta con vacantes disponibles.
 */
public class CupoSinCapacidadException extends RuntimeException {

    public CupoSinCapacidadException(String mensaje) {
        super(mensaje);
    }
}

