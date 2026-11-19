package pe.utec.consular.pagos.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Comando para registrar y confirmar un pago de trámite con clave de idempotencia.
 */
public record RegistrarPagoCommand(
        UUID tramiteId,
        BigDecimal monto,
        String claveIdempotencia
) {
}

