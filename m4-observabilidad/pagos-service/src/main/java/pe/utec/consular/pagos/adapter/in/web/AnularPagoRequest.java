package pe.utec.consular.pagos.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud JSON para anular un pago confirmado ({@code POST /api/pagos/{id}/anulacion}).
 */
public record AnularPagoRequest(
        @NotBlank(message = "El motivo de anulación es obligatorio")
        @Schema(description = "Motivo de la anulación del pago", example = "Compensación por falla de emisión documental")
        String motivo
) {
}

