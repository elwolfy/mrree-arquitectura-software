package pe.utec.consular.notificaciones.adapter.in.messaging;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Carga útil publicada por {@code citas-service} con routing key {@code cita.reservada}
 * (relación Conformista frente al contrato de eventos de la sección 6.1).
 */
public record CitaReservadaEvent(
        UUID citaId,
        String codigo,
        String dni,
        String email,
        String oficinaId,
        LocalDate fecha,
        LocalTime hora
) {
}

