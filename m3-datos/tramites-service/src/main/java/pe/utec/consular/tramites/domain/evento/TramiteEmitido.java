package pe.utec.consular.tramites.domain.evento;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio que registra la emisión exitosa del documento consular,
 * transicionando el trámite de {@code EN_EMISION} a {@code EMITIDO}.
 */
public record TramiteEmitido(
        UUID tramiteId,
        String numeroDocumento,
        Instant ocurridoEn
) implements EventoDominioTramite {

    public static final String TIPO = "TramiteEmitido";

    public TramiteEmitido {
        Objects.requireNonNull(tramiteId, "El tramiteId no puede ser nulo");
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException("El número de documento emitido no puede ser vacío");
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
        datos.put("numeroDocumento", numeroDocumento);
        return datos;
    }
}

