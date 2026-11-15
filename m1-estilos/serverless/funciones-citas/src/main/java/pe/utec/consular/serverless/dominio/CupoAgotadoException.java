package pe.utec.consular.serverless.dominio;

import java.time.LocalDate;
import java.time.LocalTime;

public class CupoAgotadoException extends RuntimeException {
    public CupoAgotadoException(String oficinaId, LocalDate fecha, LocalTime hora) {
        super("El cupo para la oficina %s en la fecha %s a las %s no tiene plazas disponibles"
                .formatted(oficinaId, fecha, hora));
    }
}

