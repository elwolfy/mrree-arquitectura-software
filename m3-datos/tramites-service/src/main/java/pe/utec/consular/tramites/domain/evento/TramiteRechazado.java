package pe.utec.consular.tramites.domain.evento;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio que marca el trámite en estado {@code RECHAZADO} con un motivo de rechazo
 * (utilizado en la compensación de la saga de la Asignación A3).
 */
public record TramiteRechazado(
        UUID tramiteId,
        String motivo,
        Instant ocurridoEn
) implements EventoDominioTramite {

    public static final String TIPO = "TramiteRechazado";

    public TramiteRechazado {
        Objects.requireNonNull(tramiteId, "El tramiteId no puede ser nulo");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo no puede ser vacío");
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
        datos.put("motivo", motivo);
        return datos;
    }
}

