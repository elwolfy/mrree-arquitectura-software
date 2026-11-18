package pe.utec.consular.citas.adapter.in.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de documentación OpenAPI 3 y reglas CORS (regla 02).
 */
@Configuration
public class OpenApiCorsConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI consularCitasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plataforma Consular — citas-service (Módulo 2)")
                        .description("API REST del Bounded Context Citas con publicación de eventos hacia RabbitMQ")
                        .version("1.0.0"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}

