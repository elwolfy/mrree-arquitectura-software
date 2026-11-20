package pe.utec.consular.tramites.domain.evento;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio que registra el envío de la orden de emisión del documento consular,
 * transicionando el trámite de {@code PAGADO} a {@code EN_EMISION}.
 */
public record EmisionSolicitada(
        UUID tramiteId,
        Instant ocurridoEn
) implements EventoDominioTramite {

    public static final String TIPO = "EmisionSolicitada";

    public EmisionSolicitada {
        Objects.requireNonNull(tramiteId, "El tramiteId no puede ser nulo");
        ocurridoEn = ocurridoEn != null ? ocurridoEn : Instant.now();
    }

    @Override
    public String tipo() {
        return TIPO;
    }

    @Override
    public Map<String, Object> aMapaDatos() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("estadoDestino", "EN_EMISION");
        return datos;
    }
}

