package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.utec.consular.citas.domain.EstadoCita;
import pe.utec.consular.citas.domain.TipoTramite;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Representación HTTP de una cita consular según contrato global (sección 4.1).
 */
@Schema(description = "Datos completos de la cita consular")
public record CitaDto(
        @Schema(description = "Identificador único de la cita (UUID)", example = "6f1c7d24-3f11-4f9e-bc43-2287da23a2a1")
        UUID id,

        @Schema(description = "Código visible correlativo de cita", example = "CIT-2026-000123")
        String codigo,

        @Schema(description = "DNI del ciudadano", example = "40000001")
        String dni,

        @Schema(description = "Nombres completos", example = "Ana Pérez")
        String nombres,

        @Schema(description = "Oficina consular de atención", example = "MAD")
        String oficinaId,

        @Schema(description = "Fecha de la cita", example = "2026-11-30")
        LocalDate fecha,

        @Schema(description = "Hora de la cita", example = "08:00")
        LocalTime hora,

        @Schema(description = "Tipo de trámite", example = "LEGALIZACION")
        TipoTramite tipoTramite,

        @Schema(description = "Estado de la cita", example = "RESERVADA")
        EstadoCita estado,

        @Schema(description = "Estado del envío de la notificación", example = "PENDIENTE")
        String notificacion,

        @Schema(description = "Fecha y hora de creación de la cita (ISO-8601)", example = "2026-11-24T19:05:00Z")
        Instant creadaEn
) {
}

