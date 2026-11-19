package pe.utec.consular.pagos.application.port.in;

import java.util.UUID;

/**
 * Comando para solicitar la anulación de un pago confirmado (Andamiaje de la Asignación A3).
 */
public record AnularPagoCommand(
        UUID pagoId,
        String motivo
) {
}

