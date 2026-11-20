package pe.utec.consular.tramites;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio tramites-service (Módulo 2).
 * <p>Concepto: Patrones creacionales en el dominio (Factory Method y Builder),
 * publicación de eventos en RabbitMQ ({@code tramite.registrado}) y comparación
 * de protocolos síncronos REST (:8082) vs. gRPC (:9082).
 * <p>Sesión: S2 y S3.
 */
@SpringBootApplication
public class TramitesApplication {

    public static void main(String[] args) {
        SpringApplication.run(TramitesApplication.class, args);
    }
}

