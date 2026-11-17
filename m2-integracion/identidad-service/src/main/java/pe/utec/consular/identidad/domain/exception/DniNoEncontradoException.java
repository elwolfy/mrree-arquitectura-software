package pe.utec.consular.identidad.domain.exception;

/**
 * Excepción lanzada cuando el DNI consultado no existe en el registro oficial de identidad.
 * Se traduce a HTTP 404 Not Found ("DNI no encontrado").
 */
public class DniNoEncontradoException extends RuntimeException {

    public DniNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

