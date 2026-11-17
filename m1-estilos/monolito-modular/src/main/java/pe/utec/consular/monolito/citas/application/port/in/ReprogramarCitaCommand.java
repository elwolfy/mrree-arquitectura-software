package pe.utec.consular.monolito.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Comando de entrada para reprogramar una cita consular hacia una nueva fecha y hora.
 * <p>Concepto: Arquitectura hexagonal – comando del puerto primario (sección 1.3).
 * <p>Sesión: S2 (Asignación A1).
 */
public record ReprogramarCitaCommand(
        UUID citaId,
        LocalDate fecha,
        LocalTime hora
) {
}

