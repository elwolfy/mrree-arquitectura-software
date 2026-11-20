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
import pe.utec.consular.tramites.application.saga.SagaSolicitudTramite;
import pe.utec.consular.tramites.domain.CodigoTramite;
import pe.utec.consular.tramites.domain.Solicitud;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.TramiteFactory;
import pe.utec.consular.tramites.domain.TramiteRegistrado;

import java.time.Year;

/**
 * Caso de uso para registrar un nuevo trámite consular con <b>Event Sourcing</b>,
 * proyección síncrona <b>CQRS</b>, inicio de <b>Saga Orquestada</b> y publicación
 * garantizada mediante <b>Transactional Outbox</b>.
 */
@Service
public class RegistrarTramiteService implements RegistrarTramiteUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarTramiteService.class);
    private static final String ROUTING_KEY_TRAMITE_REGISTRADO = "tramite.registrado";

    private final EventoTramiteStorePort eventoTramiteStorePort;
    private final ProyectorVistaTramite proyectorVistaTramite;
    private final ExpedienteRepositoryPort expedienteRepositoryPort;
    private final OutboxPort outboxPort;
    private final SagaSolicitudTramite sagaSolicitudTramite;

    public RegistrarTramiteService(EventoTramiteStorePort eventoTramiteStorePort,
                                   ProyectorVistaTramite proyectorVistaTramite,
                                   ExpedienteRepositoryPort expedienteRepositoryPort,
                                   OutboxPort outboxPort,
                                   SagaSolicitudTramite sagaSolicitudTramite) {
        this.eventoTramiteStorePort = eventoTramiteStorePort;
        this.proyectorVistaTramite = proyectorVistaTramite;
        this.expedienteRepositoryPort = expedienteRepositoryPort;
        this.outboxPort = outboxPort;
        this.sagaSolicitudTramite = sagaSolicitudTramite;
    }

    @Override
    @Transactional
    public Tramite registrar(RegistrarTramiteCommand comando) {
        log.info("Iniciando registro de trámite tipo {} para país {}", comando.tipoTramite(), comando.pais());

        Solicitud solicitud = Solicitud.builder()
                .dni(comando.dni())
                .nombres(comando.nombres())
                .email(comando.email())
                .tipo(comando.tipoTramite())
                .pais(comando.pais())
                .folios(comando.folios())
                .idioma(comando.idioma())
                .build();

        long secuencia = eventoTramiteStorePort.siguienteSecuenciaCodigo();
        CodigoTramite codigo = CodigoTramite.generar(Year.now().getValue(), secuencia);

        Tramite tramite = TramiteFactory.crear(comando.tipoTramite(), solicitud, codigo);
        tramite.pasarAPendientePago();

        eventoTramiteStorePort.guardarEventos(
                tramite.getId(),
                tramite.getVersionBase(),
                tramite.getEventosPendientes()
        );
        tramite.marcarCambiosConfirmados();

        proyectorVistaTramite.proyectar(tramite);
        expedienteRepositoryPort.guardarExpediente(tramite.getId(), tramite.construirMetadatosExpediente());
        sagaSolicitudTramite.iniciarSaga(tramite.getId());

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

        log.info("Trámite {} ({}) registrado para DNI {} en estado {} (version={}, saga=ESPERANDO_PAGO)",
                tramite.getCodigo().valor(),
                tramite.getSubtipo(),
                tramite.getDni().enmascarado(),
                tramite.getEstado(),
                tramite.getVersion());

        return tramite;
    }
}
