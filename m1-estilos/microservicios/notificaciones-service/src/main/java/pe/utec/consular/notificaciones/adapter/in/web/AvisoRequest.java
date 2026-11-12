package pe.utec.consular.notificaciones.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud HTTP para el envío de un aviso consular (`POST /api/avisos`).
 * <p>Concepto: Contrato HTTP interno entre microservicios en M1.
 */
@Schema(description = "Solicitud de despacho de aviso consular")
public record AvisoRequest(
        @NotBlank(message = "El destinatario es obligatorio")
        @Email(message = "El formato del correo del destinatario no es válido")
        @Schema(description = "Correo electrónico del ciudadano", example = "ana@correo.lab")
        String destinatario,

        @NotBlank(message = "El asunto es obligatorio")
        @Schema(description = "Asunto de la notificación", example = "Confirmación de Cita Consular - CIT-2026-000001")
        String asunto,

        @NotBlank(message = "El cuerpo del mensaje es obligatorio")
        @Schema(description = "Contenido del aviso", example = "Su cita ha sido reservada.")
        String cuerpo,

        @NotBlank(message = "La referencia es obligatoria")
        @Schema(description = "Código de referencia asociado (ej. código de cita)", example = "CIT-2026-000001")
        String referencia
) {
}

