package pe.utec.consular.bff.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración OpenAPI y pasarela transparente de errores RFC 9457 provenientes de los microservicios.
 */
@Configuration
@RestControllerAdvice
public class OpenApiAndErrorConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI bffOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plataforma Consular — API BFF Web (M2)")
                        .description("Backend For Frontend con agregación paralela y patrón Facade")
                        .version("1.0.0"));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> propagarErrorRemoto(RestClientResponseException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ex.getResponseBodyAsString());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/bff/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}

