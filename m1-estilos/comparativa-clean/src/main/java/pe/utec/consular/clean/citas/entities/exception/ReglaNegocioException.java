package pe.utec.consular.clean.citas.entities.exception;

/**
 * Excepción lanzada ante el incumplimiento de una regla de negocio general.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String message) {
        super(message);
    }
}

