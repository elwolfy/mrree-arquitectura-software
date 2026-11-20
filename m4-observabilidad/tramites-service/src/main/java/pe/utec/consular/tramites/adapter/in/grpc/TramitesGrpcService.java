package pe.utec.consular.tramites.adapter.in.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.in.ConsultarTramiteUseCase;
import pe.utec.consular.tramites.domain.VistaTramite;
import pe.utec.consular.tramites.grpc.v1.ConsultarTramiteRequest;
import pe.utec.consular.tramites.grpc.v1.TramiteResponse;
import pe.utec.consular.tramites.grpc.v1.TramitesServiceGrpc;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador primario gRPC para la consulta binaria tipada de trámites consulares (:9082).
 * <p>En M3, al igual que {@code GET /api/tramites/{id}}, lee exclusivamente del modelo
 * de lectura CQRS ({@code tramites.vista_tramite}) a través de {@link ConsultarTramiteUseCase}.
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
        Optional<VistaTramite> resultado = consultarTramiteUseCase.consultarPorId(id);
        if (resultado.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("No se encontró el trámite con identificador: " + id)
                    .asRuntimeException());
            return;
        }

        VistaTramite v = resultado.get();
        TramiteResponse response = TramiteResponse.newBuilder()
                .setId(v.tramiteId().toString())
                .setCodigo(v.codigo())
                .setDni(v.dni())
                .setNombres(v.nombres())
                .setEmail(v.email())
                .setTipoTramite(v.tipoTramite().name())
                .setSubtipo(v.subtipo())
                .setPais(v.pais())
                .setFolios(v.folios())
                .setIdioma(v.idioma())
                .setEstado(v.estado().name())
                .setMonto(v.monto().toPlainString())
                .setCreadoEn(v.creadoEn().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
