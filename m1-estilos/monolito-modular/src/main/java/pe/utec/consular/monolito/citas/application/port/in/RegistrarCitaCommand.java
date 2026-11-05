package pe.utec.consular.monolito.citas.application.port.in;

import pe.utec.consular.monolito.compartido.domain.TipoTramite;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Comando para registrar una nueva cita consular.
 * <p>Concepto: Patrón Command / Caso de uso (sección 1.3).
 * <p>Sesión: S1.
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
