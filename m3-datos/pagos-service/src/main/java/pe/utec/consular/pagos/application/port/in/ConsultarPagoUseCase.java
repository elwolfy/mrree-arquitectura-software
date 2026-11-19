package pe.utec.consular.pagos.application.port.in;

import pe.utec.consular.pagos.domain.OrdenDePago;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para consultar órdenes de pago por identificador o por trámite.
 */
public interface ConsultarPagoUseCase {

    Optional<OrdenDePago> consultarPorId(UUID pagoId);

    List<OrdenDePago> listarPorTramiteId(UUID tramiteId);
}

