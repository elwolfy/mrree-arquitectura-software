package pe.utec.consular.tramites.domain.evento;

import java.util.Objects;

/**
 * Envoltorio que asocia un {@link EventoDominioTramite} con su número de versión
 * secuencial dentro del flujo de eventos del agregado ({@code 1..N}).
 */
public record EventoVersionadoTramite(
        int version,
        EventoDominioTramite evento
) {
    public EventoVersionadoTramite {
        if (version <= 0) {
            throw new IllegalArgumentException("La versión del evento debe ser mayor que cero");
        }
        Objects.requireNonNull(evento, "El evento de dominio no puede ser nulo");
    }
}

