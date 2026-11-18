package pe.utec.consular.tramites.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteCommand;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteUseCase;
import pe.utec.consular.tramites.application.port.out.PublicadorDeEventosPort;
import pe.utec.consular.tramites.application.port.out.TramiteRepositoryPort;
import pe.utec.consular.tramites.domain.CodigoTramite;
import pe.utec.consular.tramites.domain.Solicitud;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.TramiteFactory;
import pe.utec.consular.tramites.domain.TramiteRegistrado;

import java.time.Year;

/**
 * Caso de uso para registrar un nuevo trámite consular.
 * <p>Concepto: Orquestación hexagonal apoyada en los patrones Builder ({@link Solicitud})
 * y Factory Method ({@link TramiteFactory}), delegando toda creación polimórfica a la fábrica
 * del dominio y publicando el evento {@code tramite.registrado}.
 * <p>Sesión: S2 y S3.
 */
@Service
public class RegistrarTramiteService implements RegistrarTramiteUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarTramiteService.class);

    private final TramiteRepositoryPort tramiteRepositoryPort;
    private final PublicadorDeEventosPort publicadorDeEventosPort;

    public RegistrarTramiteService(TramiteRepositoryPort tramiteRepositoryPort,
                                   PublicadorDeEventosPort publicadorDeEventosPort) {
        this.tramiteRepositoryPort = tramiteRepositoryPort;
        this.publicadorDeEventosPort = publicadorDeEventosPort;
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
        long secuencia = tramiteRepositoryPort.siguienteSecuencia();
        CodigoTramite codigo = CodigoTramite.generar(Year.now().getValue(), secuencia);

        // 3. Instanciar el subtipo concreto mediante Factory Method (estado inicial REGISTRADO)
        Tramite tramite = TramiteFactory.crear(comando.tipoTramite(), solicitud, codigo);

        // 4. Transicionar a PENDIENTE_PAGO en M2
        tramite.pasarAPendientePago();

        // 5. Persistir el trámite
        Tramite tramiteGuardado = tramiteRepositoryPort.guardar(tramite);

        log.info("Trámite {} ({}) registrado para DNI {} en estado {}",
                tramiteGuardado.getCodigo().valor(),
                tramiteGuardado.getSubtipo(),
                tramiteGuardado.getDni().enmascarado(),
                tramiteGuardado.getEstado());

        // 6. Publicar evento de integración tramite.registrado (sección 6.1 del contrato)
        publicadorDeEventosPort.publicarTramiteRegistrado(new TramiteRegistrado(
                tramiteGuardado.getId(),
                tramiteGuardado.getCodigo().valor(),
                tramiteGuardado.getDni().valor(),
                tramiteGuardado.getEmail().valor(),
                tramiteGuardado.getTipoTramite()
        ));

        return tramiteGuardado;
    }
}

