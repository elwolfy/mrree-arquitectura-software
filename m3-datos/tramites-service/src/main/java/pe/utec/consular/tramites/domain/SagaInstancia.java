package pe.utec.consular.tramites.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Estado persistido de una instancia de la saga orquestada {@code SagaSolicitudTramite}
 * en la tabla {@code tramites.saga_instancia}.
 */
public record SagaInstancia(
        UUID tramiteId,
        String paso,
        String estado,
        Instant actualizadoEn
) {
    public SagaInstancia {
        Objects.requireNonNull(tramiteId, "El tramiteId de la saga no puede ser nulo");
        Objects.requireNonNull(paso, "El paso de la saga no puede ser nulo");
        Objects.requireNonNull(estado, "El estado de la saga no puede ser nulo");
        actualizadoEn = actualizadoEn != null ? actualizadoEn : Instant.now();
    }
}

