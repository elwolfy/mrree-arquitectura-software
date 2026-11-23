package pe.utec.consular.tramites.application.port.out;

import java.util.UUID;

/**
 * Puerto secundario para solicitar la anulación compensatoria de un pago confirmado
 * hacia {@code pagos-service} ({@code POST /api/pagos/{id}/anulacion}).
 * <p>Concepto: Acción compensatoria dentro de una Saga Orquestada (Andamiaje de la Asignación A3).
 * <p>Sesión: S4 y S5.
 */
public interface AnulacionPagoPort {

    void solicitarAnulacion(UUID pagoId, String motivo);
}

