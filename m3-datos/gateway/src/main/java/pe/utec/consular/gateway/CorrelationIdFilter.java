package pe.utec.consular.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Filtro de borde que garantiza que toda solicitud que ingresa por el API Gateway
 * posea un {@code X-Correlation-Id} (UUID), lo registre en el MDC de {@code m2-gateway},
 * lo propague en las cabeceras hacia el servicio destino y lo devuelva en la respuesta HTTP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String existingHeader = request.getHeader(HEADER_CORRELATION_ID);
        final String correlationId = (existingHeader != null && !existingHeader.isBlank())
                ? existingHeader.trim()
                : UUID.randomUUID().toString();

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_CORRELATION_ID, correlationId);

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (HEADER_CORRELATION_ID.equalsIgnoreCase(name)) {
                    return correlationId;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (HEADER_CORRELATION_ID.equalsIgnoreCase(name)) {
                    return Collections.enumeration(List.of(correlationId));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> names = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
                names.add(HEADER_CORRELATION_ID);
                return Collections.enumeration(names);
            }
        };

        try {
            log.info("Gateway enrutando {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}

