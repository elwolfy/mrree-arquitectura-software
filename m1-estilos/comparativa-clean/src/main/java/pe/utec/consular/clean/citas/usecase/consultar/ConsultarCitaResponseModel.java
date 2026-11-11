package pe.utec.consular.clean.citas.usecase.consultar;

import pe.utec.consular.clean.citas.entities.EstadoCita;
import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Modelo de respuesta de una cita consultada en el círculo {@code usecase}.
 * <p>Concepto: Clean Architecture – Response Model (sección 1.3 del plan).
 */
public record ConsultarCitaResponseModel(
        UUID id,
        String codigo,
        String dni,
        String nombres,
        String oficinaId,
        LocalDate fecha,
        LocalTime hora,
        TipoTramite tipoTramite,
        EstadoCita estado,
        String notificacion,
        Instant creadaEn
) {
}

