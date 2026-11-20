package pe.utec.consular.tramites.domain.evento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio que registra la confirmación y asociación de un pago al trámite,
 * transicionando su estado de {@code PENDIENTE_PAGO} a {@code PAGADO}.
 */
public record PagoAsociado(
        UUID tramiteId,
        UUID pagoId,
        BigDecimal monto,
        Instant ocurridoEn
) implements EventoDominioTramite {

    public static final String TIPO = "PagoAsociado";

    public PagoAsociado {
        Objects.requireNonNull(tramiteId, "El tramiteId no puede ser nulo");
        Objects.requireNonNull(pagoId, "El pagoId no puede ser nulo");
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        ocurridoEn = ocurridoEn != null ? ocurridoEn : Instant.now();
    }

    @Override
    public String tipo() {
        return TIPO;
    }

    @Override
    public Map<String, Object> aMapaDatos() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("pagoId", pagoId.toString());
        datos.put("monto", monto);
        return datos;
    }
}

