package pe.utec.consular.tramites.adapter.in.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.in.ConsultarTramiteUseCase;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.grpc.v1.ConsultarTramiteRequest;
import pe.utec.consular.tramites.grpc.v1.TramiteResponse;
import pe.utec.consular.tramites.grpc.v1.TramitesServiceGrpc;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador primario gRPC para la consulta binaria tipada de trámites consulares (:9082).
 * <p>Concepto: Comparación de estilos de comunicación síncrona REST vs. gRPC (sección 2.3).
 * Invoca exactamente el mismo puerto de entrada ({@link ConsultarTramiteUseCase}) que el controlador REST.
 * <p>Sesión: S3.
 */
@Component
public class TramitesGrpcService extends TramitesServiceGrpc.TramitesServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(TramitesGrpcService.class);

    private final ConsultarTramiteUseCase consultarTramiteUseCase;

    public TramitesGrpcService(ConsultarTramiteUseCase consultarTramiteUseCase) {
        this.consultarTramiteUseCase = consultarTramiteUseCase;
    }

    @Override
    public void consultarTramite(ConsultarTramiteRequest request, StreamObserver<TramiteResponse> responseObserver) {
        UUID id;
        try {
            id = UUID.fromString(request.getId());
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Identificador UUID de trámite inválido: " + request.getId())
                    .asRuntimeException());
            return;
        }

        log.info("Consulta gRPC recibida para trámite {}", id);
        Optional<Tramite> resultado = consultarTramiteUseCase.consultarPorId(id);
        if (resultado.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("No se encontró el trámite con identificador: " + id)
                    .asRuntimeException());
            return;
        }

        Tramite t = resultado.get();
        TramiteResponse response = TramiteResponse.newBuilder()
                .setId(t.getId().toString())
                .setCodigo(t.getCodigo().valor())
                .setDni(t.getDni().valor())
                .setNombres(t.getNombres())
                .setEmail(t.getEmail().valor())
                .setTipoTramite(t.getTipoTramite().name())
                .setSubtipo(t.getSubtipo())
                .setPais(t.getPais())
                .setFolios(t.getFolios())
                .setIdioma(t.getIdioma())
                .setEstado(t.getEstado().name())
                .setMonto(t.getMonto().toPlainString())
                .setCreadoEn(t.getCreadoEn().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

