package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import pe.utec.consular.citas.domain.TipoTramite;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Solicitud HTTP para la reserva de una cita consular (`POST /api/citas`).
 */
@Schema(description = "Solicitud de reserva de una cita consular")
public record RegistrarCitaRequest(
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener exactamente 8 dígitos numéricos")
        @Schema(description = "DNI del ciudadano (8 dígitos)", example = "40000001")
        String dni,

        @NotBlank(message = "Los nombres del ciudadano son obligatorios")
        @Schema(description = "Nombres y apellidos del ciudadano", example = "Ana Pérez")
        String nombres,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Schema(description = "Correo electrónico para notificaciones", example = "ana@correo.lab")
        String email,

        @NotBlank(message = "El identificador de la oficina consular es obligatorio")
        @Schema(description = "Código de la oficina consular", example = "MAD")
        String oficinaId,

        @NotNull(message = "La fecha de la cita es obligatoria")
        @Schema(description = "Fecha deseada para la cita (YYYY-MM-DD)", example = "2026-11-30")
        LocalDate fecha,

        @NotNull(message = "La hora de la cita es obligatoria")
        @Schema(description = "Hora deseada para la cita (HH:mm)", example = "08:00")
        LocalTime hora,

        @NotNull(message = "El tipo de trámite es obligatorio")
        @Schema(description = "Tipo de trámite consular a realizar", example = "LEGALIZACION")
        TipoTramite tipoTramite
) {
}

