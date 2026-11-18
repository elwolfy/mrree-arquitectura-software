package pe.utec.consular.identidad.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Solicitud HTTP para validar la identidad de un ciudadano por DNI.
 */
@Schema(description = "Solicitud de validación de identidad ciudadana")
public record ValidarIdentidadRequest(
        @Schema(description = "Número de DNI de 8 dígitos", example = "40000001")
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener exactamente 8 dígitos numéricos")
        String dni
) {
}

