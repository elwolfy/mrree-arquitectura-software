package pe.utec.consular.serverless.funciones;

import java.time.LocalDate;
import java.time.LocalTime;
import pe.utec.consular.serverless.dominio.TipoTramite;

public record RegistrarCitaRequest(
        String dni,
        String nombres,
        String email,
        String oficinaId,
        TipoTramite tipoTramite,
        LocalDate fecha,
        LocalTime hora
) {
}

