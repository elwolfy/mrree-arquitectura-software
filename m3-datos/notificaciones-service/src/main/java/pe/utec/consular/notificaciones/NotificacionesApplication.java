package pe.utec.consular.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio notificaciones-service (Módulo 2).
 * <p>Concepto: Patrón Observer distribuido (2.1), relación Conformista en el Context Map (2.4),
 * idempotencia por {@code messageId} y manejo de mensajes fallidos con reintentos exponenciales y DLQ (2.3).
 * <p>Sesión: S3.
 */
@SpringBootApplication
public class NotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionesApplication.class, args);
    }
}

