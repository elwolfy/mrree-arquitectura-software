package pe.utec.consular.tramites.adapter.in.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de OpenAPI 3 y CORS para tramites-service.
 */
@Configuration
public class OpenApiCorsConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI tramitesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plataforma Consular — API de Trámites (M2)")
                        .description("Microservicio hexagonal con Factory Method, Builder, eventos RabbitMQ y gRPC")
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

