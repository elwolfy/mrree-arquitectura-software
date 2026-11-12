package pe.utec.consular.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal del microservicio de citas en M1 (`consular-m1-citas-service`).
 * <p>Concepto: Estilo de arquitectura de microservicios (sección 1.2 del plan).
 * Expone la API de citas de M1 en el puerto 8081 e invoca a {@code notificaciones-service}
 * mediante HTTP REST con Circuit Breaker Resilience4j.
 * <p>Sesión: S1.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class CitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }
}

