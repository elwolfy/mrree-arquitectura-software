package pe.utec.consular.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Filtro limitador de tasa (Rate Limiter) en memoria sobre {@code /api/identidad/**}.
 * <p>Permite un máximo de {@code gateway.ratelimit.identidad-por-segundo} solicitudes por ventana
 * deslizante de 1 segundo (por defecto 5 req/s), respondiendo {@code 429 Too Many Requests}
 * cuando se supera el umbral para proteger la cuota hacia la PIDE.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class IdentidadRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdentidadRateLimitFilter.class);
    private static final long VENTANA_MS = 1000L;

    private final int maxSolicitudesPorSegundo;
    private final ObjectMapper objectMapper;
    private final Deque<Long> marcasDeTiempo = new ConcurrentLinkedDeque<>();

    public IdentidadRateLimitFilter(
            @Value("${gateway.ratelimit.identidad-por-segundo:5}") int maxSolicitudesPorSegundo,
            ObjectMapper objectMapper) {
        this.maxSolicitudesPorSegundo = Math.max(1, maxSolicitudesPorSegundo);
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/api/identidad");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long ahora = System.currentTimeMillis();
        boolean permitido;

        synchronized (marcasDeTiempo) {
            while (!marcasDeTiempo.isEmpty() && (ahora - marcasDeTiempo.peekFirst()) >= VENTANA_MS) {
                marcasDeTiempo.pollFirst();
            }
            if (marcasDeTiempo.size() >= maxSolicitudesPorSegundo) {
                permitido = false;
            } else {
                marcasDeTiempo.addLast(ahora);
                permitido = true;
            }
        }

        if (!permitido) {
            log.warn("Rate limit superado en {} (máximo {} req/s)", request.getRequestURI(), maxSolicitudesPorSegundo);
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Se superó el límite de " + maxSolicitudesPorSegundo + " solicitudes por segundo hacia /api/identidad/**"
            );
            pd.setTitle("Límite de solicitudes excedido");
            pd.setType(URI.create("https://consular.lab/errores/limite-solicitudes-excedido"));
            pd.setInstance(URI.create(request.getRequestURI()));
            pd.setProperty("codigo", "limite-solicitudes-excedido");

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), pd);
            return;
        }

        filterChain.doFilter(request, response);
    }
}

