package pe.utec.consular.pagos.adapter.in.web;

import pe.utec.consular.pagos.domain.EstadoPago;
import pe.utec.consular.pagos.domain.OrdenDePago;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representación JSON de un pago consular en {@code pagos-service}.
 */
public record PagoDto(
        UUID id,
        UUID tramiteId,
        BigDecimal monto,
        String moneda,
        EstadoPago estado,
        String claveIdempotencia,
        Instant creadoEn
) {
    public static PagoDto desdeDominio(OrdenDePago orden) {
        return new PagoDto(
                orden.getId(),
                orden.getTramiteId(),
                orden.getMonto(),
                orden.getMoneda(),
                orden.getEstado(),
                orden.getClaveIdempotencia(),
                orden.getCreadaEn()
        );
    }
}

