package pe.utec.consular.pagos.application.port.out;

import pe.utec.consular.pagos.domain.OrdenDePago;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario para persistencia de órdenes de pago en {@code pagos.pago}.
 */
public interface PagoRepositoryPort {

    OrdenDePago guardar(OrdenDePago ordenDePago);

    Optional<OrdenDePago> buscarPorId(UUID pagoId);

    Optional<OrdenDePago> buscarPorClaveIdempotencia(String claveIdempotencia);

    List<OrdenDePago> buscarPorTramiteId(UUID tramiteId);
}

