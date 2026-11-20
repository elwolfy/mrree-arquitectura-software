package pe.utec.consular.emision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de emisión documental {@code emision-service} (:8087).
 */
@SpringBootApplication
public class EmisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmisionApplication.class, args);
    }
}

