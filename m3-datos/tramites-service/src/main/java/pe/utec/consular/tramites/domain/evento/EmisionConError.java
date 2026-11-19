package pe.utec.consular.tramites.domain.evento;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio que deja constancia de un error reportado por {@code emision-service}
 * mientras el trámite permanece en {@code EN_EMISION} (comportamiento en {@code main} antes
 * de la compensación de la Asignación A3).
 */
public record EmisionConError(
        UUID tramiteId,
        String motivo,
        Instant ocurridoEn
) implements EventoDominioTramite {

    public static final String TIPO = "EmisionConError";

    public EmisionConError {
        Objects.requireNonNull(tramiteId, "El tramiteId no puede ser nulo");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del error de emisión no puede ser vacío");
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

