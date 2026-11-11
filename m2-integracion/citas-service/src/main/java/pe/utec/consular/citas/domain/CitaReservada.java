package pe.utec.consular.citas.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Evento de integración publicado cuando se reserva una cita consular (routing key {@code cita.reservada}).
 * <p>Concepto: Lenguaje publicado (Published Language) e integración orientada a eventos (secciones 2.3 y 2.4).
 * <p>Sesión: S3.
 */
public record CitaReservada(
        UUID citaId,
        String codigo,
        String dni,
        String email,
        String oficinaId,
        LocalDate fecha,
        LocalTime hora
) {
}

