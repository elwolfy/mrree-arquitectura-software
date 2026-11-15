package pe.utec.consular.serverless.dominio;

import java.util.UUID;

public class TransicionEstadoInvalidaException extends RuntimeException {
    public TransicionEstadoInvalidaException(UUID citaId, EstadoCita actual, EstadoCita destino) {
        super("No es posible cambiar la cita %s del estado %s a %s".formatted(citaId, actual, destino));
    }
}

