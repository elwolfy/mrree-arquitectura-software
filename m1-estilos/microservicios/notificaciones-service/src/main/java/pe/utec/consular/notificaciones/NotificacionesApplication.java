package pe.utec.consular.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Aplicación principal del microservicio de notificaciones en M1 (`consular-m1-notificaciones-service`).
 * <p>Concepto: Estilo de arquitectura de microservicios (sección 1.2 del plan).
 * Expone los avisos consulares como un proceso independiente con su propio esquema PostgreSQL (`notificaciones`).
 * <p>Sesión: S1.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class NotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionesApplication.class, args);
    }
}

