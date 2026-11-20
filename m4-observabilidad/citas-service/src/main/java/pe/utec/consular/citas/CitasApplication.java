package pe.utec.consular.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del microservicio de citas consulares (Módulo 2).
 * <p>Concepto: Microservicio por Bounded Context con mensajería asíncrona (secciones 2.3 y 2.4).
 * <p>Sesión: S3.
 */
@SpringBootApplication
public class CitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }
}

