package pe.utec.consular.pagos.application.port.in;

import pe.utec.consular.pagos.domain.OrdenDePago;

/**
 * Puerto de entrada para anular un pago confirmado (Andamiaje de la Asignación A3).
 */
public interface AnularPagoUseCase {

    OrdenDePago anularPago(AnularPagoCommand comando);
}

