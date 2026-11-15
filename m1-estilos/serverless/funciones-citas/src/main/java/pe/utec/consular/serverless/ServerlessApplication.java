package pe.utec.consular.serverless;

import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ServerlessApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerlessApplication.class);

    @Value("${spring.cloud.function.definition:todas}")
    private String definicionFunciones;

    public static void main(String[] args) {
        SpringApplication.run(ServerlessApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registrarTiempoArranqueEnFrio() {
        long tiempoArranqueMs = ManagementFactory.getRuntimeMXBean().getUptime();
        log.info("Función lista en {} ms [definición activa: {}]", tiempoArranqueMs, definicionFunciones);
    }
}

