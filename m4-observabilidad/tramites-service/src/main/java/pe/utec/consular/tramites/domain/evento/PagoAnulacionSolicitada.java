package pe.utec.consular.tramites.domain.evento;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio que registra la solicitud de compensación (anulación del pago)
 * dentro de la saga de solicitud de trámite (Asignación A3).
 */
public record PagoAnulacionSolicitada(
        UUID tramiteId,
        UUID pagoId,
        String motivo,
        Instant ocurridoEn
) implements EventoDominioTramite {

    public static final String TIPO = "PagoAnulacionSolicitada";

    public PagoAnulacionSolicitada {
        Objects.requireNonNull(tramiteId, "El tramiteId no puede ser nulo");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de anulación de pago no puede ser vacío");
        }
        ocurridoEn = ocurridoEn != null ? ocurridoEn : Instant.now();
    }

    @Override
    public String tipo() {
        return TIPO;
    }

    @Override
    public Map<String, Object> aMapaDatos() {
        Map<String, Object> datos = new LinkedHashMap<>();
        if (pagoId != null) {
            datos.put("pagoId", pagoId.toString());
        }
        datos.put("motivo", motivo);
        return datos;
    }
}

