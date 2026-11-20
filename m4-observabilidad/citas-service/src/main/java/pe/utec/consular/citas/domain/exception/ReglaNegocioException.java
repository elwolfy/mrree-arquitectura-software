package pe.utec.consular.citas.domain.exception;

/**
 * Excepción base para infracciones a las reglas de negocio del dominio de citas.
 */
public class ReglaNegocioException extends IllegalArgumentException {

    public ReglaNegocioException(String message) {
        super(message);
    }
}

