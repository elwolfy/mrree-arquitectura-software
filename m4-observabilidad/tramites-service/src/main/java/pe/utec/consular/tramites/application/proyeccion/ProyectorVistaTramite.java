package pe.utec.consular.tramites.application.proyeccion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.ReconstruirVistaTramiteUseCase;
import pe.utec.consular.tramites.application.port.out.CacheConsultaTramitePort;
import pe.utec.consular.tramites.application.port.out.EventoTramiteStorePort;
import pe.utec.consular.tramites.application.port.out.VistaTramiteRepositoryPort;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.VistaTramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Proyector CQRS encargado de mantener actualizada la tabla de lectura {@code tramites.vista_tramite}
 * e invalidar el caché de consulta ({@link CacheConsultaTramitePort}) ante cualquier cambio de estado.
 */
@Component
public class ProyectorVistaTramite implements ReconstruirVistaTramiteUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProyectorVistaTramite.class);

    private final VistaTramiteRepositoryPort vistaTramiteRepositoryPort;
    private final EventoTramiteStorePort eventoTramiteStorePort;
    private final CacheConsultaTramitePort cacheConsultaTramitePort;

    public ProyectorVistaTramite(VistaTramiteRepositoryPort vistaTramiteRepositoryPort,
                                 EventoTramiteStorePort eventoTramiteStorePort,
                                 CacheConsultaTramitePort cacheConsultaTramitePort) {
        this.vistaTramiteRepositoryPort = vistaTramiteRepositoryPort;
        this.eventoTramiteStorePort = eventoTramiteStorePort;
        this.cacheConsultaTramitePort = cacheConsultaTramitePort;
    }

    @Transactional
    public VistaTramite proyectar(Tramite tramite) {
        VistaTramite vista = tramite.aVista();
        VistaTramite guardada = vistaTramiteRepositoryPort.guardar(vista);
        cacheConsultaTramitePort.invalidar(tramite.getId());
        log.debug("Proyección CQRS vista_tramite actualizada y caché invalidado para trámite {} (estado={}, version={})",
                tramite.getCodigo().valor(), tramite.getEstado(), tramite.getVersion());
        return guardada;
    }

    @Override
    @Transactional
    public int reconstruir() {
        log.info("Iniciando reconstrucción completa de la proyección CQRS tramites.vista_tramite desde eventos_tramite...");
        vistaTramiteRepositoryPort.eliminarTodas();

        Map<UUID, List<EventoVersionadoTramite>> flujos = eventoTramiteStorePort.obtenerTodosLosFlujosDeEventos();
        int reconstruidos = 0;

        for (Map.Entry<UUID, List<EventoVersionadoTramite>> entry : flujos.entrySet()) {
            List<EventoVersionadoTramite> eventos = entry.getValue();
            if (!eventos.isEmpty()) {
                Tramite tramiteReconstruido = Tramite.reconstruirDesdeEventosVersionados(eventos);
                vistaTramiteRepositoryPort.guardar(tramiteReconstruido.aVista());
                cacheConsultaTramitePort.invalidar(tramiteReconstruido.getId());
                reconstruidos++;
            }
        }

        log.info("Reconstrucción CQRS finalizada: {} trámites proyectados en tramites.vista_tramite", reconstruidos);
        return reconstruidos;
    }
}
