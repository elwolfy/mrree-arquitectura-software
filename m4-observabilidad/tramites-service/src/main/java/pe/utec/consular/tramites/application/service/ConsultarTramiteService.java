package pe.utec.consular.tramites.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.ConsultarHistorialTramiteUseCase;
import pe.utec.consular.tramites.application.port.in.ConsultarTramiteUseCase;
import pe.utec.consular.tramites.application.port.out.CacheConsultaTramitePort;
import pe.utec.consular.tramites.application.port.out.EventoTramiteStorePort;
import pe.utec.consular.tramites.application.port.out.VistaTramiteRepositoryPort;
import pe.utec.consular.tramites.domain.ResultadoConsultaTramite;
import pe.utec.consular.tramites.domain.VistaTramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;
import pe.utec.consular.tramites.domain.exception.RecursoNoEncontradoException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de aplicación para consultas de trámite:
 * <ul>
 *   <li>{@link #consultarConOrigenCache(UUID)} aplica el patrón <b>Cache-Aside</b> utilizando
 *       {@link CacheConsultaTramitePort} delante de la proyección CQRS {@code tramites.vista_tramite}.</li>
 *   <li>{@link #consultarHistorial(UUID)} lee la secuencia ordenada de eventos desde {@code tramites.eventos_tramite}.</li>
 * </ul>
 */
@Service
public class ConsultarTramiteService implements ConsultarTramiteUseCase, ConsultarHistorialTramiteUseCase {

    private final VistaTramiteRepositoryPort vistaTramiteRepositoryPort;
    private final EventoTramiteStorePort eventoTramiteStorePort;
    private final CacheConsultaTramitePort cacheConsultaTramitePort;

    public ConsultarTramiteService(VistaTramiteRepositoryPort vistaTramiteRepositoryPort,
                                   EventoTramiteStorePort eventoTramiteStorePort,
                                   CacheConsultaTramitePort cacheConsultaTramitePort) {
        this.vistaTramiteRepositoryPort = vistaTramiteRepositoryPort;
        this.eventoTramiteStorePort = eventoTramiteStorePort;
        this.cacheConsultaTramitePort = cacheConsultaTramitePort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VistaTramite> consultarPorId(UUID id) {
        return consultarConOrigenCache(id).map(ResultadoConsultaTramite::vista);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultadoConsultaTramite> consultarConOrigenCache(UUID id) {
        Optional<VistaTramite> enCache = cacheConsultaTramitePort.obtener(id);
        if (enCache.isPresent()) {
            return Optional.of(new ResultadoConsultaTramite(enCache.get(), true));
        }

        Optional<VistaTramite> enVista = vistaTramiteRepositoryPort.buscarPorId(id);
        enVista.ifPresent(vista -> cacheConsultaTramitePort.guardar(id, vista));
        return enVista.map(vista -> new ResultadoConsultaTramite(vista, false));
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
