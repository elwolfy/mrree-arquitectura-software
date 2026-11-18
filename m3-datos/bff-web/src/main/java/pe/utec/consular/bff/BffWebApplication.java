package pe.utec.consular.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Backend For Frontend web (bff-web, puerto 8085).
 * <p>Concepto: Patrón BFF (2.2) con agregación paralela resiliente y patrón estructural Facade (2.1).
 * Invoca a los microservicios directamente por nombre de contenedor en la red interna, nunca a través del API Gateway.
 * <p>Sesión: S3.
 */
@SpringBootApplication
public class BffWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffWebApplication.class, args);
    }
}

