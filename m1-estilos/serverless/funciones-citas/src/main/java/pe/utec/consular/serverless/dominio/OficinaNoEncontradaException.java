package pe.utec.consular.serverless.dominio;

public class OficinaNoEncontradaException extends RuntimeException {
    public OficinaNoEncontradaException(String oficinaId) {
        super("No existe la oficina consular con código: " + oficinaId);
    }
}

