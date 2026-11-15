package pe.utec.consular.serverless.infraestructura;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroHttpFunciones extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final ManejadorGlobalErrores manejadorGlobalErrores;

    public FiltroHttpFunciones(ObjectMapper objectMapper, ManejadorGlobalErrores manejadorGlobalErrores) {
        this.objectMapper = objectMapper;
        this.manejadorGlobalErrores = manejadorGlobalErrores;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        HttpServletRequest requestAProcesar = adaptarMetodoFuncionSiCorresponde(request, uri);
        HttpServletResponse responseAProcesar = uri.endsWith("/cancelarCita")
                ? new CancelarCitaResponseWrapper(response)
                : response;

        try {
            filterChain.doFilter(requestAProcesar, responseAProcesar);
            if (uri.endsWith("/cancelarCita")
                    && !response.isCommitted()
                    && (response.getStatus() == HttpServletResponse.SC_OK
                            || response.getStatus() == HttpServletResponse.SC_ACCEPTED)) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (Throwable ex) {
            Throwable causa = desenrollarCausa(ex);
            ProblemDetail problem = manejadorGlobalErrores.construirProblemDetail(causa);
            response.resetBuffer();
            response.setStatus(problem.getStatus());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getOutputStream(), problem);
        }
    }

    private HttpServletRequest adaptarMetodoFuncionSiCorresponde(HttpServletRequest request, String uri) {
        boolean esFuncionConMensaje = uri.endsWith("/consultarDisponibilidad")
                || uri.endsWith("/consultarCita")
                || uri.endsWith("/cancelarCita");

        if (esFuncionConMensaje && ("GET".equalsIgnoreCase(request.getMethod()) || "DELETE".equalsIgnoreCase(request.getMethod()))) {
            return new PostJsonRequestWrapper(request);
        }
        return request;
    }

    private Throwable desenrollarCausa(Throwable ex) {
        Throwable actual = ex;
        while (actual.getCause() != null && actual.getCause() != actual) {
            if (actual instanceof pe.utec.consular.serverless.dominio.OficinaNoEncontradaException
                    || actual instanceof pe.utec.consular.serverless.dominio.CitaNoEncontradaException
                    || actual instanceof pe.utec.consular.serverless.dominio.CupoAgotadoException
                    || actual instanceof pe.utec.consular.serverless.dominio.CitaDuplicadaException
                    || actual instanceof pe.utec.consular.serverless.dominio.ReglaReservaVioladaException
                    || actual instanceof pe.utec.consular.serverless.dominio.TransicionEstadoInvalidaException
                    || actual instanceof IllegalArgumentException) {
                return actual;
            }
            actual = actual.getCause();
        }
        return actual;
    }

    private static class CancelarCitaResponseWrapper extends HttpServletResponseWrapper {

        CancelarCitaResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            if (sc == HttpServletResponse.SC_OK || sc == HttpServletResponse.SC_ACCEPTED) {
                super.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                super.setStatus(sc);
            }
        }
    }

    private static class PostJsonRequestWrapper extends HttpServletRequestWrapper {

        private static final byte[] CUERPO_VACIO_JSON = "{}".getBytes(StandardCharsets.UTF_8);

        PostJsonRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getMethod() {
            return "POST";
        }

        @Override
        public String getContentType() {
            return MediaType.APPLICATION_JSON_VALUE;
        }

        @Override
        public int getContentLength() {
            return CUERPO_VACIO_JSON.length;
        }

        @Override
        public long getContentLengthLong() {
            return CUERPO_VACIO_JSON.length;
        }

        @Override
        public String getHeader(String name) {
            if ("Content-Type".equalsIgnoreCase(name)) {
                return MediaType.APPLICATION_JSON_VALUE;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("Content-Type".equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(MediaType.APPLICATION_JSON_VALUE));
            }
            return super.getHeaders(name);
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(CUERPO_VACIO_JSON);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }

                @Override
                public int read() {
                    return bais.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
