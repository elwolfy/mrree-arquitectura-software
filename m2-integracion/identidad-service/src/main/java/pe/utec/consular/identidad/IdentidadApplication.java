package pe.utec.consular.identidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio identidad-service (Módulo 2).
 * <p>Concepto: Patrón estructural Adapter (2.1) y Capa Anticorrupción (ACL) en el Context Map (2.4)
 * para aislar el dominio consular del contrato externo de RENIEC en la PIDE simulada.
 * <p>Sesión: S3.
 */
@SpringBootApplication
public class IdentidadApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentidadApplication.class, args);
    }
}

