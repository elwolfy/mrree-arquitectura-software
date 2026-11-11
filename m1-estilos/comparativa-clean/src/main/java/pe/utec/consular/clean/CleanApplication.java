package pe.utec.consular.clean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.utec.consular.clean.citas.frameworks.config.ConsistenciaProperties;
import pe.utec.consular.clean.citas.frameworks.config.NotificacionesProperties;

/**
 * Aplicación principal de la variante Clean Architecture del Módulo 1.
 * <p>Concepto: Clean Architecture de Robert C. Martin vs. Arquitectura Hexagonal (sección 1.3 del plan).
 * Expone exactamente la misma API REST en el puerto 8081 que {@code consular-monolito}, pero organiza
 * su diseño interno en círculos concéntricos ({@code entities}, {@code usecase}, {@code interfaceadapters},
 * {@code frameworks}) con flujo Output Boundary -> Presenter -> ViewModel.
 * <p>Sesión: S2.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ConsistenciaProperties.class, NotificacionesProperties.class})
public class CleanApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanApplication.class, args);
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

