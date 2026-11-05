package pe.utec.consular.monolito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.utec.consular.monolito.compartido.infraestructura.ConsistenciaProperties;
import pe.utec.consular.monolito.compartido.infraestructura.NotificacionesProperties;

/**
 * Aplicación principal del Monolito Modular Consular.
 * <p>Concepto: Variante Monolito Modular en la comparativa de estilos arquitectónicos (sección 1.2 del plan).
 * Arquitectura interna hexagonal (1.3) estructurada con Spring Modulith.
 * <p>Sesión: S1 y S2.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ConsistenciaProperties.class, NotificacionesProperties.class})
public class MonolitoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonolitoApplication.class, args);
    }

    /**
     * Configuración CORS para admitir el frontend Angular en http://localhost:4200 según regla 02.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
