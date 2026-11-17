package pe.utec.consular.monolito.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cuerpo de la solicitud HTTP para reprogramar una cita consular existente.
 */
@Schema(description = "Solicitud de reprogramación de cita consular")
public record ReprogramarCitaRequest(
        @Schema(description = "Nueva fecha deseada (YYYY-MM-DD)", example = "2026-11-30")
        @NotNull(message = "La nueva fecha es obligatoria")
        LocalDate fecha,

        @Schema(description = "Nueva hora deseada (HH:mm)", example = "10:00")
        @NotNull(message = "La nueva hora es obligatoria")
        LocalTime hora
) {
}

