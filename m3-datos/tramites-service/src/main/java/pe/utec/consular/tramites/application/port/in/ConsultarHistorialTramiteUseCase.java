package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada para consultar el historial ordenado de eventos de dominio
 * ({@code GET /api/tramites/{id}/historial}) almacenado en {@code tramites.eventos_tramite}.
 */
public interface ConsultarHistorialTramiteUseCase {

    List<EventoVersionadoTramite> consultarHistorial(UUID tramiteId);
}

