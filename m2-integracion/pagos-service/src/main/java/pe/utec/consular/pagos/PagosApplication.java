package pe.utec.consular.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio pagos-service (Módulo 2).
 * <p>Concepto: Patrón de comportamiento Strategy (2.1) con {@code List<TarifaStrategy>}
 * y distinción semántica del Lenguaje Ubicuo por Bounded Context ({@code OrdenDePago}).
 * <p>Sesión: S2 y S3.
 */
@SpringBootApplication
public class PagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosApplication.class, args);
    }
}

