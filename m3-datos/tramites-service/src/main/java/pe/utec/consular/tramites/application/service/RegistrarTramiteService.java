package pe.utec.consular.tramites.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteCommand;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteUseCase;
import pe.utec.consular.tramites.application.port.out.EventoTramiteStorePort;
import pe.utec.consular.tramites.application.port.out.ExpedienteRepositoryPort;
import pe.utec.consular.tramites.application.port.out.OutboxPort;
import pe.utec.consular.tramites.application.proyeccion.ProyectorVistaTramite;
import pe.utec.consular.tramites.domain.CodigoTramite;
import pe.utec.consular.tramites.domain.Solicitud;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.TramiteFactory;
import pe.utec.consular.tramites.domain.TramiteRegistrado;

import java.time.Year;

/**
 * Caso de uso para registrar un nuevo trámite consular con <b>Event Sourcing</b>,
 * proyección síncrona <b>CQRS</b> y publicación garantizada mediante <b>Transactional Outbox</b>.
 * <p>En una única transacción local ACID:
 * <ol>
 *   <li>Construye la solicitud con Builder y el agregado polimórfico con Factory Method.</li>
 *   <li>Emite el evento de dominio {@code TramiteRegistrado} (versión 1) y lo inserta en {@code tramites.eventos_tramite}.</li>
 *   <li>Actualiza el modelo de lectura {@code tramites.vista_tramite} mediante {@link ProyectorVistaTramite}.</li>
 *   <li>Guarda el documento variable JSONB en {@code tramites.expediente}.</li>
 *   <li>Registra el evento de integración {@code tramite.registrado} en {@code tramites.outbox} para que {@code RelayOutbox} lo publique en RabbitMQ.</li>
 * </ol>
 * <p>Sesión: S2, S3, S4 y S5.
 */
@Service
public class RegistrarTramiteService implements RegistrarTramiteUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarTramiteService.class);
    private static final String ROUTING_KEY_TRAMITE_REGISTRADO = "tramite.registrado";

    private final EventoTramiteStorePort eventoTramiteStorePort;
    private final ProyectorVistaTramite proyectorVistaTramite;
    private final ExpedienteRepositoryPort expedienteRepositoryPort;
    private final OutboxPort outboxPort;

    public RegistrarTramiteService(EventoTramiteStorePort eventoTramiteStorePort,
                                   ProyectorVistaTramite proyectorVistaTramite,
                                   ExpedienteRepositoryPort expedienteRepositoryPort,
                                   OutboxPort outboxPort) {
        this.eventoTramiteStorePort = eventoTramiteStorePort;
        this.proyectorVistaTramite = proyectorVistaTramite;
        this.expedienteRepositoryPort = expedienteRepositoryPort;
        this.outboxPort = outboxPort;
    }

    @Override
    @Transactional
    public Tramite registrar(RegistrarTramiteCommand comando) {
        log.info("Iniciando registro de trámite tipo {} para país {}", comando.tipoTramite(), comando.pais());

        // 1. Construir la solicitud inmutable validando coherencia con el patrón Builder
        Solicitud solicitud = Solicitud.builder()
                .dni(comando.dni())
                .nombres(comando.nombres())
                .email(comando.email())
                .tipo(comando.tipoTramite())
                .pais(comando.pais())
                .folios(comando.folios())
                .idioma(comando.idioma())
                .build();

        // 2. Generar código correlativo (TRM-YYYY-NNNNNN)
        long secuencia = eventoTramiteStorePort.siguienteSecuenciaCodigo();
        CodigoTramite codigo = CodigoTramite.generar(Year.now().getValue(), secuencia);

        // 3. Instanciar el subtipo concreto mediante Factory Method (estado inicial REGISTRADO, versión 0)
        Tramite tramite = TramiteFactory.crear(comando.tipoTramite(), solicitud, codigo);

        // 4. Emitir evento de dominio TramiteRegistrado (versión 1) -> estado PENDIENTE_PAGO
        tramite.pasarAPendientePago();

        // 5. Persistir eventos en tramites.eventos_tramite con control de concurrencia optimista
        eventoTramiteStorePort.guardarEventos(
                tramite.getId(),
                tramite.getVersionBase(),
                tramite.getEventosPendientes()
        );
        tramite.marcarCambiosConfirmados();

        // 6. Proyectar en tramites.vista_tramite en la misma transacción
        proyectorVistaTramite.proyectar(tramite);

        // 7. Guardar documento JSONB en tramites.expediente
        expedienteRepositoryPort.guardarExpediente(tramite.getId(), tramite.construirMetadatosExpediente());

        // 8. Insertar evento de integración tramite.registrado en tramites.outbox (Transactional Outbox)
        outboxPort.registrarEnOutbox(
                tramite.getId(),
                ROUTING_KEY_TRAMITE_REGISTRADO,
                new TramiteRegistrado(
                        tramite.getId(),
                        tramite.getCodigo().valor(),
                        tramite.getDni().valor(),
                        tramite.getEmail().valor(),
                        tramite.getTipoTramite()
                )
        );

        log.info("Trámite {} ({}) registrado para DNI {} en estado {} (version={}, outbox=pendiente)",
                tramite.getCodigo().valor(),
                tramite.getSubtipo(),
                tramite.getDni().enmascarado(),
                tramite.getEstado(),
                tramite.getVersion());

        return tramite;
    }
}
