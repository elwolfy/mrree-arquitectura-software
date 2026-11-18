package pe.utec.consular.tramites.adapter.in.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Componente que gestiona el arranque y apagado limpio del servidor gRPC en el puerto configurado
 * (por defecto {@code 9082}).
 */
@Component
public class GrpcServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final TramitesGrpcService tramitesGrpcService;
    private final int configuredPort;
    private Server server;

    public GrpcServerLifecycle(TramitesGrpcService tramitesGrpcService,
                               @Value("${grpc.port:9082}") int configuredPort) {
        this.tramitesGrpcService = tramitesGrpcService;
        this.configuredPort = configuredPort;
    }

    @Override
    public void start() {
        try {
            this.server = ServerBuilder.forPort(configuredPort)
                    .addService(tramitesGrpcService)
                    .build()
                    .start();
            log.info("Servidor gRPC (consular.tramites.v1.TramitesService) iniciado en el puerto {}", server.getPort());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo iniciar el servidor gRPC en el puerto " + configuredPort, ex);
        }
    }

    @Override
    public void stop() {
        if (this.server != null) {
            log.info("Deteniendo servidor gRPC...");
            this.server.shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return this.server != null && !this.server.isShutdown();
    }

    public int getPort() {
        return this.server != null ? this.server.getPort() : configuredPort;
    }
}

