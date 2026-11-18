package pe.utec.consular.tramites.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la solicitud HTTP para observar un trámite consular (Asignación A2).
 */
@Schema(description = "Solicitud de observación de un trámite consular")
public record ObservarTramiteRequest(
        @Schema(description = "Motivo de la observación que el ciudadano debe subsanar", example = "La copia del documento presenta sello ilegible en el reverso")
        @NotBlank(message = "El motivo de observación es obligatorio")
        String motivo
) {
}

