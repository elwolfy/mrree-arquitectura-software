package pe.utec.consular.serverless.dominio;

public class ReglaReservaVioladaException extends RuntimeException {
    public ReglaReservaVioladaException(String mensaje) {
        super(mensaje);
    }
}

