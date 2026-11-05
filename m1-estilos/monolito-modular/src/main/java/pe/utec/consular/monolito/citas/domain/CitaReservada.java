package pe.utec.consular.monolito.citas.domain;

import pe.utec.consular.monolito.compartido.domain.CitaReservadaInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Evento de dominio publicado tras completarse la reserva de una cita.
 * <p>Concepto: Eventos de dominio y desacoplamiento mediante Patrón Observer (sección 2.1 del plan).
 * <p>Sesión: S2.
 */
public record CitaReservada(
        UUID citaId,
        String codigo,
        String dni,
        String email,
        String oficinaId,
        LocalDate fecha,
        LocalTime hora
) implements CitaReservadaInfo {
}
