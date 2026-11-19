package pe.utec.consular.tramites.application.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.out.AnulacionPagoPort;
import pe.utec.consular.tramites.application.port.out.EventoTramiteStorePort;
import pe.utec.consular.tramites.application.port.out.OutboxPort;
import pe.utec.consular.tramites.application.port.out.SagaInstanciaRepositoryPort;
import pe.utec.consular.tramites.application.proyeccion.ProyectorVistaTramite;
import pe.utec.consular.tramites.domain.EmisionCompletada;
import pe.utec.consular.tramites.domain.EmisionFallida;
import pe.utec.consular.tramites.domain.EstadoTramite;
import pe.utec.consular.tramites.domain.PagoConfirmado;
import pe.utec.consular.tramites.domain.SagaInstancia;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orquestador central de la <b>Saga Solicitud → Pago → Emisión</b> ({@code SagaSolicitudTramite}).
 *
 * <h3>¿Qué es una Saga Orquestada y dónde poner puntos de parada (breakpoints) en Eclipse?</h3>
 * <p>En una arquitectura de microservicios con <i>Database-per-Service</i> no existen transacciones
 * distribuidas (2PC) entre {@code tramites-service}, {@code pagos-service} y {@code emision-service}.
 * La consistencia entre ellos se logra mediante una <b>Saga Orquestada</b>: una secuencia de
 * transacciones locales coordinadas por un único componente ({@code SagaSolicitudTramite}) que mantiene
 * el estado explícito de cada instancia en {@code tramites.saga_instancia}:
 * <ol>
 *   <li><b>Paso 1 — {@link #iniciarSaga(UUID)} (Breakpoint 1):</b> Invocado al finalizar {@code POST /api/tramites}.
 *       El trámite nace en {@link EstadoTramite#PENDIENTE_PAGO} y la saga queda en paso {@code ESPERANDO_PAGO}.</li>
 *   <li><b>Paso 2 — {@link #alConfirmarPago(PagoConfirmado)} (Breakpoint 2):</b> Al recibir {@code pago.confirmado}
 *       desde RabbitMQ, reconstruye el agregado {@link Tramite} desde {@code eventos_tramite}, registra
 *       {@code PagoAsociado} ({@code PAGADO}) y {@code EmisionSolicitada} ({@code EN_EMISION}), encola el comando
 *       {@code emision.solicitar} en {@code tramites.outbox}, proyecta {@code vista_tramite} y avanza
 *       {@code saga_instancia} a {@code EMISION_SOLICITADA}.</li>
 *   <li><b>Paso 3a — {@link #alCompletarEmision(EmisionCompletada)} (Breakpoint 3a, camino feliz):</b>
 *       Al recibir {@code emision.completada}, registra {@code TramiteEmitido} ({@code EMITIDO}) con el
 *       número de documento {@code DOC-YYYY-NNNNNN}, proyecta {@code vista_tramite} y marca la saga como
 *       {@code COMPLETADA}.</li>
 *   <li><b>Paso 3b — {@link #alFallarEmision(EmisionFallida)} (Breakpoint 3b, Asignación A3):</b>
 *       Al recibir {@code emision.fallida}, en {@code main} solo emite una advertencia dejando el trámite
 *       en {@code EN_EMISION}. En la <b>Asignación A3</b> se completa este método para ejecutar la
 *       compensación semántica (anular el pago vía {@link AnulacionPagoPort} y rechazar el trámite con
 *       {@code TramiteRechazado}).</li>
 * </ol>
 * <p>Todos los manejadores son <b>idempotentes</b>: si RabbitMQ reentrega un evento ya aplicado sobre el
 * agregado, el orquestador detecta que el trámite ya superó ese estado y descarta el mensaje sin duplicar eventos.
 *
 * <p>Conceptos: Saga Orquestada, Event Sourcing, CQRS, Transactional Outbox e Idempotencia (sección 3.3).
 * <p>Sesión: S4 y S5.
 */
@Component
public class SagaSolicitudTramite {

    private static final Logger log = LoggerFactory.getLogger(SagaSolicitudTramite.class);
    private static final String ROUTING_KEY_EMISION_SOLICITAR = "emision.solicitar";

    private final EventoTramiteStorePort eventoTramiteStorePort;
    private final ProyectorVistaTramite proyectorVistaTramite;
    private final OutboxPort outboxPort;
    private final SagaInstanciaRepositoryPort sagaInstanciaRepositoryPort;
    @SuppressWarnings("unused")
    private final AnulacionPagoPort anulacionPagoPort;

    public SagaSolicitudTramite(EventoTramiteStorePort eventoTramiteStorePort,
                                ProyectorVistaTramite proyectorVistaTramite,
                                OutboxPort outboxPort,
                                SagaInstanciaRepositoryPort sagaInstanciaRepositoryPort,
                                AnulacionPagoPort anulacionPagoPort) {
        this.eventoTramiteStorePort = eventoTramiteStorePort;
        this.proyectorVistaTramite = proyectorVistaTramite;
        this.outboxPort = outboxPort;
        this.sagaInstanciaRepositoryPort = sagaInstanciaRepositoryPort;
        this.anulacionPagoPort = anulacionPagoPort;
    }

    /**
     * Paso 1: Registra el inicio de la saga cuando el trámite queda en {@code PENDIENTE_PAGO}.
     */
    @Transactional
    public void iniciarSaga(UUID tramiteId) {
        sagaInstanciaRepositoryPort.guardar(new SagaInstancia(
                tramiteId,
                "ESPERANDO_PAGO",
                EstadoTramite.PENDIENTE_PAGO.name(),
                Instant.now()
        ));
    }

    /**
     * Paso 2: Procesa de manera idempotente el evento {@code pago.confirmado}, transiciona el trámite
     * por {@code PAGADO} hacia {@code EN_EMISION} y envía el comando {@code emision.solicitar} vía Outbox.
     */
    @Transactional
    public void alConfirmarPago(PagoConfirmado evento) {
        List<EventoVersionadoTramite> historial = eventoTramiteStorePort.obtenerEventosPorTramiteId(evento.tramiteId());
        if (historial.isEmpty()) {
            log.warn("Saga recibió pago.confirmado para trámite inexistente {}; ignorando mensaje", evento.tramiteId());
            return;
        }

        Tramite tramite = Tramite.reconstruirDesdeEventosVersionados(historial);
        if (tramite.getEstado() != EstadoTramite.PENDIENTE_PAGO) {
            log.info("Saga ignoró pago.confirmado repetido para trámite {} (estado actual={})",
                    tramite.getId(), tramite.getEstado());
            return;
        }

        int versionBase = tramite.getVersionBase();
        tramite.asociarPago(evento.pagoId(), evento.monto());
        tramite.solicitarEmision();

        eventoTramiteStorePort.guardarEventos(tramite.getId(), versionBase, tramite.getEventosPendientes());
        tramite.marcarCambiosConfirmados();

        proyectorVistaTramite.proyectar(tramite);

        Map<String, Object> comandoEmision = new LinkedHashMap<>();
        comandoEmision.put("tramiteId", tramite.getId().toString());
        comandoEmision.put("codigo", tramite.getCodigo().valor());
        comandoEmision.put("tipoTramite", tramite.getTipoTramite().name());
        outboxPort.registrarEnOutbox(tramite.getId(), ROUTING_KEY_EMISION_SOLICITAR, comandoEmision);

        sagaInstanciaRepositoryPort.guardar(new SagaInstancia(
                tramite.getId(),
                "EMISION_SOLICITADA",
                EstadoTramite.EN_EMISION.name(),
                Instant.now()
        ));

        log.info("Saga avanzó trámite {} a EN_EMISION tras confirmar pago {} y encoló emision.solicitar en outbox",
                tramite.getCodigo().valor(), evento.pagoId());
    }

    /**
     * Paso 3a (Camino feliz): Procesa de manera idempotente {@code emision.completada} y culmina
     * el trámite en {@code EMITIDO}.
     */
    @Transactional
    public void alCompletarEmision(EmisionCompletada evento) {
        List<EventoVersionadoTramite> historial = eventoTramiteStorePort.obtenerEventosPorTramiteId(evento.tramiteId());
        if (historial.isEmpty()) {
            log.warn("Saga recibió emision.completada para trámite inexistente {}; ignorando mensaje", evento.tramiteId());
            return;
        }

        Tramite tramite = Tramite.reconstruirDesdeEventosVersionados(historial);
        if (tramite.getEstado() == EstadoTramite.EMITIDO) {
            log.info("Saga ignoró emision.completada repetido para trámite {} (ya está EMITIDO)", tramite.getId());
            return;
        }
        if (tramite.getEstado() != EstadoTramite.EN_EMISION) {
            log.warn("Saga recibió emision.completada para trámite {} en estado inesperado {}",
                    tramite.getId(), tramite.getEstado());
            return;
        }

        int versionBase = tramite.getVersionBase();
        tramite.marcarEmitido(evento.numeroDocumento());

        eventoTramiteStorePort.guardarEventos(tramite.getId(), versionBase, tramite.getEventosPendientes());
        tramite.marcarCambiosConfirmados();

        proyectorVistaTramite.proyectar(tramite);

        sagaInstanciaRepositoryPort.guardar(new SagaInstancia(
                tramite.getId(),
                "EMISION_COMPLETADA",
                EstadoTramite.EMITIDO.name(),
                Instant.now()
        ));

        log.info("Saga completada exitosamente para trámite {}: estado EMITIDO con documento {}",
                tramite.getCodigo().valor(), evento.numeroDocumento());
    }

    /**
     * Paso 3b (Falla de emisión — Punto de extensión de la Asignación A3).
     */
    @Transactional
    public void alFallarEmision(EmisionFallida evento) {
        // TODO Asignación A3 – Tarea 2: compensar la saga.
        // 1) Solicitar la anulación del pago (POST /api/pagos/{id}/anulacion, ya implementado en pagos-service).
        // 2) Registrar TramiteRechazado con el motivo recibido (estado RECHAZADO y motivo_rechazo).
        log.warn("Sin compensación: ver asignación A3. Trámite {} queda EN_EMISION", evento.tramiteId());
    }
}

