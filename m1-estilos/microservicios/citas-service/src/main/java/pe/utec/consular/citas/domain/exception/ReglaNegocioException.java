package pe.utec.consular.citas.domain.exception;

/**
 * Excepción lanzada ante violaciones de invariantes o reglas de negocio del dominio.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

