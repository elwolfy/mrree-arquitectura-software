package pe.utec.consular.tramites.adapter.in.web;

import java.time.Instant;
import java.util.Map;

/**
 * Representación JSON de un evento del flujo de Event Sourcing devuelto por
 * {@code GET /api/tramites/{id}/historial}.
 */
public record EventoHistorialDto(
        String tipo,
        int version,
        Instant fecha,
        Instant ocurridoEn,
        Map<String, Object> datos
) {
}

