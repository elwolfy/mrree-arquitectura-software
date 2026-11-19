package pe.utec.consular.tramites.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.ConsultarHistorialTramiteUseCase;
import pe.utec.consular.tramites.application.port.in.ConsultarTramiteUseCase;
import pe.utec.consular.tramites.application.port.out.EventoTramiteStorePort;
import pe.utec.consular.tramites.application.port.out.VistaTramiteRepositoryPort;
import pe.utec.consular.tramites.domain.VistaTramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;
import pe.utec.consular.tramites.domain.exception.RecursoNoEncontradoException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de aplicación para consultas de trámite:
 * <ul>
 *   <li>{@link #consultarPorId(UUID)} lee <b>exclusivamente</b> de la proyección CQRS {@code tramites.vista_tramite}.</li>
 *   <li>{@link #consultarHistorial(UUID)} lee la secuencia ordenada de eventos desde {@code tramites.eventos_tramite}.</li>
 * </ul>
 */
@Service
public class ConsultarTramiteService implements ConsultarTramiteUseCase, ConsultarHistorialTramiteUseCase {

    private final VistaTramiteRepositoryPort vistaTramiteRepositoryPort;
    private final EventoTramiteStorePort eventoTramiteStorePort;

    public ConsultarTramiteService(VistaTramiteRepositoryPort vistaTramiteRepositoryPort,
                                   EventoTramiteStorePort eventoTramiteStorePort) {
        this.vistaTramiteRepositoryPort = vistaTramiteRepositoryPort;
        this.eventoTramiteStorePort = eventoTramiteStorePort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VistaTramite> consultarPorId(UUID id) {
        return vistaTramiteRepositoryPort.buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoVersionadoTramite> consultarHistorial(UUID tramiteId) {
        List<EventoVersionadoTramite> eventos = eventoTramiteStorePort.obtenerEventosPorTramiteId(tramiteId);
        if (eventos.isEmpty()) {
            throw new RecursoNoEncontradoException("No se encontraron eventos para el trámite con identificador: " + tramiteId);
        }
        return eventos;
    }
}
