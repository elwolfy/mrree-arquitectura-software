package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.TipoTramite;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Comando de entrada para registrar una nueva cita consular.
 */
public record RegistrarCitaCommand(
        String dni,
        String nombres,
        String email,
        String oficinaId,
        LocalDate fecha,
        LocalTime hora,
        TipoTramite tipoTramite
) {
}

