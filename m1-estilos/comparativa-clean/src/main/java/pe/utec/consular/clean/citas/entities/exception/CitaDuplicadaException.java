package pe.utec.consular.clean.citas.entities.exception;

/**
 * Excepción lanzada cuando un ciudadano intenta reservar más de una cita activa el mismo día en la misma oficina.
 */
public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(String message) {
        super(message);
    }
}

