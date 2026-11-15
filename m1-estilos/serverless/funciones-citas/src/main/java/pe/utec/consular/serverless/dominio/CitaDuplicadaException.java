package pe.utec.consular.serverless.dominio;

public class CitaDuplicadaException extends RuntimeException {
    public CitaDuplicadaException(String dni, TipoTramite tipoTramite) {
        super("El ciudadano con DNI %s ya posee una cita activa para el trámite %s"
                .formatted(dni, tipoTramite));
    }
}

