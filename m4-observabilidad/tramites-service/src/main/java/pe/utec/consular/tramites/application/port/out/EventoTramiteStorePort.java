package pe.utec.consular.tramites.application.port.out;

import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Puerto secundario hacia el Event Store ({@code tramites.eventos_tramite}).
 * <p>Garantiza almacenamiento inmutable (solo inserciones) y control de concurrencia optimista
 * verificando la versión esperada del flujo de eventos del agregado.
 */
public interface EventoTramiteStorePort {

    /**
     * Persiste los nuevos eventos del agregado verificando que la última versión almacenada
     * coincida con {@code versionEsperada}.
     *
     * @throws pe.utec.consular.tramites.domain.exception.ConflictoVersionException si la versión en BD difiere
     */
    void guardarEventos(UUID tramiteId, int versionEsperada, List<EventoVersionadoTramite> nuevosEventos);

    /**
     * Recupera el flujo ordenado de eventos ({@code version ASC}) para reconstruir un trámite o consultar su historial.
     */
    List<EventoVersionadoTramite> obtenerEventosPorTramiteId(UUID tramiteId);

    /**
     * Recupera todos los flujos de eventos agrupados por {@code tramite_id} y ordenados por {@code version ASC},
     * utilizado para reconstruir desde cero la proyección CQRS {@code tramites.vista_tramite}.
     */
    Map<UUID, List<EventoVersionadoTramite>> obtenerTodosLosFlujosDeEventos();

    /**
     * Obtiene el siguiente número correlativo de la secuencia {@code tramites.tramite_codigo_seq}.
     */
    long siguienteSecuenciaCodigo();
}

