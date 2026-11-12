package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Representación JSON de una cita consular según la sección 4.1 del contrato global.
 */
@Schema(description = "Datos de una cita consular reservada")
public record CitaDto(
        @Schema(description = "Identificador único UUID")
        UUID id,
        @Schema(description = "Código correlativo de la cita", example = "CIT-2026-000001")
        String codigo,
        @Schema(description = "DNI del ciudadano", example = "40000001")
        String dni,
        @Schema(description = "Nombres completos del ciudadano", example = "Ana Pérez")
        String nombres,
        @Schema(description = "Código de la oficina consular", example = "MAD")
        String oficinaId,
        @Schema(description = "Fecha de la cita", example = "2026-11-30")
        LocalDate fecha,
        @Schema(description = "Hora de la cita", example = "08:00")
        LocalTime hora,
        @Schema(description = "Tipo de trámite", example = "LEGALIZACION")
        String tipoTramite,
        @Schema(description = "Estado de la cita", example = "RESERVADA")
        String estado,
        @Schema(description = "Estado del envío de la notificación (ENVIADA o PENDIENTE)", example = "ENVIADA")
        String notificacion,
        @Schema(description = "Instante de creación ISO-8601")
        Instant creadaEn
) {
}

