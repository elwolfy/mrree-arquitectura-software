package pe.utec.consular.notificaciones.adapter.in.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de OpenAPI 3 y CORS para notificaciones-service.
 */
@Configuration
public class OpenApiCorsConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI notificacionesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plataforma Consular — API de Notificaciones (M2)")
                        .description("Microservicio con consumidores idempotentes en RabbitMQ, reintentos y DLQ")
                        .version("1.0.0"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}

