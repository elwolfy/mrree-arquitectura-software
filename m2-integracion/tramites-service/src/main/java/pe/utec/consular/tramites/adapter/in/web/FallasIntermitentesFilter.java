package pe.utec.consular.tramites.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Filtro HTTP didáctico para simular fallas intermitentes ({@code 503 Service Unavailable})
 * en las consultas {@code GET} de trámites.
 * <p>Concepto: Resiliencia en la malla / patrón Sidecar con Envoy (sección 2.5, prompt 08-6).
 * Controlado mediante la propiedad {@code fallas.intermitentes.porcentaje} (por defecto {@code 0},
 * variable de entorno {@code FALLAS_INTERMITENTES_PORCENTAJE}).
 */
@Component
public class FallasIntermitentesFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FallasIntermitentesFilter.class);

    private final int porcentajeFallas;
    private final ObjectMapper objectMapper;

    public FallasIntermitentesFilter(
            @Value("${fallas.intermitentes.porcentaje:0}") int porcentajeFallas,
            ObjectMapper objectMapper) {
        this.porcentajeFallas = Math.max(0, Math.min(100, porcentajeFallas));
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (porcentajeFallas <= 0) {
            return true;
        }
        String uri = request.getRequestURI();
        return !"GET".equalsIgnoreCase(request.getMethod()) || !uri.startsWith("/api/tramites");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        int tirada = ThreadLocalRandom.current().nextInt(100);
        if (tirada < porcentajeFallas) {
            log.warn("Simulando falla intermitente HTTP 503 en {} {} (porcentaje configurado: {}%)",
                    request.getMethod(), request.getRequestURI(), porcentajeFallas);
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Falla intermitente simulada en tramites-service (" + porcentajeFallas + "% configurado)"
            );
            pd.setTitle("Servicio temporalmente no disponible (falla intermitente simulada)");
            pd.setType(URI.create("https://consular.lab/errores/falla-intermitente-simulada"));
            pd.setInstance(URI.create(request.getRequestURI()));
            pd.setProperty("codigo", "falla-intermitente-simulada");

            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), pd);
            return;
        }
        filterChain.doFilter(request, response);
    }
}

