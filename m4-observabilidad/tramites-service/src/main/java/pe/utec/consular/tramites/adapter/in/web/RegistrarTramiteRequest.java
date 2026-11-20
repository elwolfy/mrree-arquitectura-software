package pe.utec.consular.tramites.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import pe.utec.consular.tramites.domain.TipoTramite;

/**
 * Cuerpo de la solicitud HTTP para iniciar un nuevo trámite consular.
 */
@Schema(description = "Solicitud de registro de trámite consular")
public record RegistrarTramiteRequest(
        @Schema(description = "Número de DNI de 8 dígitos", example = "40000001")
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener exactamente 8 dígitos numéricos")
        String dni,

        @Schema(description = "Nombres y apellidos del ciudadano", example = "Ana Pérez")
        @NotBlank(message = "El nombre del ciudadano es obligatorio")
        String nombres,

        @Schema(description = "Correo electrónico del ciudadano", example = "ana@correo.lab")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        String email,

        @Schema(description = "Tipo de trámite solicitado", example = "LEGALIZACION")
        @NotNull(message = "El tipo de trámite es obligatorio")
        TipoTramite tipoTramite,

        @Schema(description = "Código ISO de 2 letras del país destino", example = "ES")
        @NotBlank(message = "El código de país es obligatorio")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "El país debe tener un código ISO de 2 letras")
        String pais,

        @Schema(description = "Cantidad de folios del documento", example = "2")
        @Min(value = 1, message = "El número de folios debe ser al menos 1")
        int folios,

        @Schema(description = "Código ISO del idioma del documento", example = "ES")
        @NotBlank(message = "El idioma del documento es obligatorio")
        String idioma
) {
}

