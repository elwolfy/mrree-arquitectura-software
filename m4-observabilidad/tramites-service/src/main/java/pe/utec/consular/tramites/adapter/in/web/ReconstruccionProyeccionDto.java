package pe.utec.consular.tramites.adapter.in.web;

import java.time.Instant;

/**
 * Respuesta del endpoint administrativo {@code POST /admin/proyecciones/vista-tramite/reconstruir}.
 */
public record ReconstruccionProyeccionDto(
        int tramitesReconstruidos,
        Instant reconstruidoEn
) {
}

