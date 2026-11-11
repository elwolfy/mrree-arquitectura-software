package pe.utec.consular.clean.citas.interfaceadapters.presenter;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.utec.consular.clean.citas.entities.EstadoCita;
import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * ViewModel de presentación JSON de una cita consular (Círculo 3: Interface Adapters).
 * <p>Concepto: Clean Architecture – ViewModel generado por el {@link CitaPresenter} (sección 1.3 del plan).
 * Mantiene la misma estructura JSON que {@code CitaDto} en {@code monolito-modular} para cumplir el contrato global.
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.adapter.in.web.CitaDto}.
 */
@Schema(description = "Representación de una cita consular reservada (ViewModel de Clean Architecture)")
public record CitaViewModel(
        @Schema(description = "Identificador único UUID de la cita", example = "6f1c9a2e-8b10-4c55-9a21-334455667788")
        UUID id,

        @Schema(description = "Código correlativo de la cita", example = "CIT-2026-000123")
        String codigo,

        @Schema(description = "DNI del ciudadano", example = "40000001")
        String dni,

        @Schema(description = "Nombres completos del ciudadano", example = "Ana Pérez")
        String nombres,

        @Schema(description = "Código de la oficina consular", example = "MAD")
        String oficinaId,

        @Schema(description = "Fecha programada de la cita", example = "2026-11-30")
        LocalDate fecha,

        @Schema(description = "Hora programada de la cita", example = "08:00")
        LocalTime hora,

        @Schema(description = "Tipo de trámite consular", example = "LEGALIZACION")
        TipoTramite tipoTramite,

        @Schema(description = "Estado actual de la cita", example = "RESERVADA")
        EstadoCita estado,

        @Schema(description = "Estado del envío de la notificación", example = "ENVIADA")
        String notificacion,

        @Schema(description = "Fecha y hora de creación en formato ISO-8601", example = "2026-11-24T19:05:00Z")
        Instant creadaEn
) {
}

