package pe.utec.consular.serverless.dominio;

import java.util.UUID;

public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(UUID citaId) {
        super("No existe la cita con identificador: " + citaId);
    }
}

