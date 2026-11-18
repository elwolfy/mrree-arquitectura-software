package pe.utec.consular.identidad.domain.exception;

/**
 * Excepción lanzada cuando la identidad del ciudadano no se encuentra vigente
 * (por ejemplo, registro con restricción por fallecimiento).
 * Se traduce a HTTP 422 Unprocessable Entity ("identidad no vigente").
 */
public class IdentidadNoVigenteException extends RuntimeException {

    public IdentidadNoVigenteException(String mensaje) {
        super(mensaje);
    }
}

