package pe.utec.consular.pagos.application.port.in;

import pe.utec.consular.pagos.domain.OrdenDePago;

/**
 * Puerto de entrada para el registro idempotente de un pago consular.
 */
public interface RegistrarPagoUseCase {

    OrdenDePago registrarPago(RegistrarPagoCommand comando);
}

