package pe.utec.consular.tramites.domain.evento;

import pe.utec.consular.tramites.domain.TipoTramite;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento inicial del flujo de Event Sourcing cuando se registra un trámite consular
 * y queda en estado {@code PENDIENTE_PAGO}.
 */
public record TramiteRegistrado(
        UUID tramiteId,
        String codigo,
        String dni,
        String nombres,
        String email,
        TipoTramite tipoTramite,
        String pais,
        int folios,
        String idioma,
        BigDecimal monto,
        Instant ocurridoEn
) implements EventoDominioTramite {

    public static final String TIPO = "TramiteRegistrado";

    public TramiteRegistrado {
        Objects.requireNonNull(tramiteId, "El tramiteId no puede ser nulo");
        Objects.requireNonNull(codigo, "El código no puede ser nulo");
        Objects.requireNonNull(dni, "El DNI no puede ser nulo");
        Objects.requireNonNull(nombres, "Los nombres no pueden ser nulos");
        Objects.requireNonNull(email, "El email no puede ser nulo");
        Objects.requireNonNull(tipoTramite, "El tipo de trámite no puede ser nulo");
        Objects.requireNonNull(pais, "El país no puede ser nulo");
        Objects.requireNonNull(idioma, "El idioma no puede ser nulo");
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
        datos.put("codigo", codigo);
        datos.put("dni", dni);
        datos.put("nombres", nombres);
        datos.put("email", email);
        datos.put("tipoTramite", tipoTramite.name());
        datos.put("pais", pais);
        datos.put("folios", folios);
        datos.put("idioma", idioma);
        datos.put("monto", monto);
        return datos;
    }
}

