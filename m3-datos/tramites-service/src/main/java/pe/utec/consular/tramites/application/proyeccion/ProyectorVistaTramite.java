package pe.utec.consular.tramites.application.proyeccion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.ReconstruirVistaTramiteUseCase;
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
 * a partir del estado derivado de los eventos de dominio del agregado {@link Tramite}.
 *
 * <h3>Patrón CQRS (Command Query Responsibility Segregation) con Event Sourcing</h3>
 * <ul>
 *   <li><b>Lado de Escritura (Command):</b> Valida reglas de negocio sobre el agregado {@link Tramite}
 *       reconstruido desde {@code tramites.eventos_tramite} y apendiza nuevos eventos inmutables.</li>
 *   <li><b>Proyección:</b> En la misma transacción local, {@code ProyectorVistaTramite} actualiza
 *       la fila desnormalizada en {@code tramites.vista_tramite} (incluyendo {@code motivo_rechazo}
 *       y {@code actualizado_en}).</li>
 *   <li><b>Lado de Lectura (Query):</b> {@code GET /api/tramites/{id}} y {@code GET /api/tramites?dni=}
 *       consultan exclusivamente {@code tramites.vista_tramite} sin tener que reproducir eventos en cada lectura.</li>
 *   <li><b>Reconstrucción determinista:</b> Si la vista se corrompe o evoluciona su estructura,
 *       {@link #reconstruir()} borra {@code tramites.vista_tramite} y la regenera íntegramente
 *       reproduciendo todos los eventos desde {@code tramites.eventos_tramite}.</li>
 * </ul>
 * <p>Sesión: S4 y S5.
 */
@Component
public class ProyectorVistaTramite implements ReconstruirVistaTramiteUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProyectorVistaTramite.class);

    private final VistaTramiteRepositoryPort vistaTramiteRepositoryPort;
    private final EventoTramiteStorePort eventoTramiteStorePort;

    public ProyectorVistaTramite(VistaTramiteRepositoryPort vistaTramiteRepositoryPort,
                                 EventoTramiteStorePort eventoTramiteStorePort) {
        this.vistaTramiteRepositoryPort = vistaTramiteRepositoryPort;
        this.eventoTramiteStorePort = eventoTramiteStorePort;
    }

    /**
     * Proyecta el estado actual del agregado {@link Tramite} hacia {@code tramites.vista_tramite}
     * dentro de la transacción activa que persiste los eventos de dominio.
     */
    @Transactional
    public VistaTramite proyectar(Tramite tramite) {
        VistaTramite vista = tramite.aVista();
        VistaTramite guardada = vistaTramiteRepositoryPort.guardar(vista);
        log.debug("Proyección CQRS vista_tramite actualizada para trámite {} (estado={}, version={})",
                tramite.getCodigo().valor(), tramite.getEstado(), tramite.getVersion());
        return guardada;
    }

    /**
     * Borra por completo {@code tramites.vista_tramite} y la reconstruye determinísticamente
     * reproduciendo cada flujo de eventos desde {@code tramites.eventos_tramite}.
     *
     * @return número total de trámites reconstruidos en la vista
     */
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
                reconstruidos++;
            }
        }

        log.info("Reconstrucción CQRS finalizada: {} trámites proyectados en tramites.vista_tramite", reconstruidos);
        return reconstruidos;
    }
}

