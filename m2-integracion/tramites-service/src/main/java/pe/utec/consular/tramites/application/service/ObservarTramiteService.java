package pe.utec.consular.tramites.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.ObservarTramiteCommand;
import pe.utec.consular.tramites.application.port.in.ObservarTramiteUseCase;
import pe.utec.consular.tramites.application.port.out.PublicadorDeEventosPort;
import pe.utec.consular.tramites.application.port.out.TramiteRepositoryPort;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.TramiteObservado;
import pe.utec.consular.tramites.domain.exception.RecursoNoEncontradoException;

/**
 * Caso de uso para marcar un trámite en estado {@code OBSERVADO} y publicar el evento
 * {@code tramite.observado} en RabbitMQ (Andamiaje de la Asignación A2).
 */
@Service
public class ObservarTramiteService implements ObservarTramiteUseCase {

    private static final Logger log = LoggerFactory.getLogger(ObservarTramiteService.class);

    private final TramiteRepositoryPort tramiteRepositoryPort;
    private final PublicadorDeEventosPort publicadorDeEventosPort;

    public ObservarTramiteService(TramiteRepositoryPort tramiteRepositoryPort,
                                  PublicadorDeEventosPort publicadorDeEventosPort) {
        this.tramiteRepositoryPort = tramiteRepositoryPort;
        this.publicadorDeEventosPort = publicadorDeEventosPort;
    }

    @Override
    @Transactional
    public Tramite observar(ObservarTramiteCommand comando) {
        Tramite tramite = tramiteRepositoryPort.buscarPorId(comando.tramiteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el trámite con identificador: " + comando.tramiteId()));

        tramite.observar(comando.motivo());
        Tramite guardado = tramiteRepositoryPort.guardar(tramite);

        log.info("Trámite {} pasado a estado OBSERVADO por motivo: {}",
                guardado.getCodigo().valor(), comando.motivo());

        publicadorDeEventosPort.publicarTramiteObservado(new TramiteObservado(
                guardado.getId(),
                guardado.getCodigo().valor(),
                guardado.getDni().valor(),
                guardado.getEmail().valor(),
                comando.motivo().trim()
        ));

        return guardado;
    }
}

