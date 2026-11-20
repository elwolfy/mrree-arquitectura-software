package pe.utec.consular.pagos.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Solicitud JSON para registrar un pago consular ({@code POST /api/pagos}).
 */
public record RegistrarPagoRequest(
        @NotNull(message = "El identificador del trámite es obligatorio")
        @Schema(description = "UUID del trámite consular a pagar", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID tramiteId,

        @NotNull(message = "El monto a pagar es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        @Schema(description = "Monto arancelario en PEN", example = "33.00")
        BigDecimal monto
) {
}

