package pe.utec.consular.serverless.infraestructura;

import java.net.URI;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import pe.utec.consular.serverless.dominio.CitaDuplicadaException;
import pe.utec.consular.serverless.dominio.CitaNoEncontradaException;
import pe.utec.consular.serverless.dominio.CupoAgotadoException;
import pe.utec.consular.serverless.dominio.OficinaNoEncontradaException;
import pe.utec.consular.serverless.dominio.ReglaReservaVioladaException;
import pe.utec.consular.serverless.dominio.TransicionEstadoInvalidaException;
import pe.utec.consular.serverless.funciones.CitaDto;

@RestControllerAdvice
public class ManejadorGlobalErrores implements ResponseBodyAdvice<Object> {

    private static final String BASE_PROBLEMA_URI = "https://consular.utec.edu.pe/errores/";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        String path = request.getURI().getPath();
        if (path != null && path.endsWith("/reservarCita") && body instanceof CitaDto citaDto) {
            response.setStatusCode(HttpStatus.CREATED);
            response.getHeaders().set(HttpHeaders.LOCATION, "/api/citas/" + citaDto.id());
        } else if (path != null && path.endsWith("/cancelarCita") && !(body instanceof ProblemDetail)) {
            response.setStatusCode(HttpStatus.NO_CONTENT);
            return null;
        }
        return body;
    }

    @ExceptionHandler(OficinaNoEncontradaException.class)
    public ProblemDetail manejarOficinaNoEncontrada(OficinaNoEncontradaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Oficina consular no encontrada");
        problem.setType(URI.create(BASE_PROBLEMA_URI + "oficina-no-encontrada"));
        return problem;
    }

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ProblemDetail manejarCitaNoEncontrada(CitaNoEncontradaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Cita no encontrada");
        problem.setType(URI.create(BASE_PROBLEMA_URI + "cita-no-encontrada"));
        return problem;
    }

    @ExceptionHandler({CupoAgotadoException.class, ObjectOptimisticLockingFailureException.class})
    public ProblemDetail manejarCupoAgotado(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Cupo agotado");
        problem.setType(URI.create(BASE_PROBLEMA_URI + "cupo-agotado"));
        return problem;
    }

    @ExceptionHandler(CitaDuplicadaException.class)
    public ProblemDetail manejarCitaDuplicada(CitaDuplicadaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Cita duplicada");
        problem.setType(URI.create(BASE_PROBLEMA_URI + "cita-duplicada"));
        return problem;
    }

    @ExceptionHandler({ReglaReservaVioladaException.class, TransicionEstadoInvalidaException.class})
    public ProblemDetail manejarReglaNegocio(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Regla de negocio violada");
        problem.setType(URI.create(BASE_PROBLEMA_URI + "regla-reserva-violada"));
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail manejarArgumentoInvalido(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Datos de entrada inválidos");
        problem.setType(URI.create(BASE_PROBLEMA_URI + "datos-invalidos"));
        return problem;
    }

    public ProblemDetail construirProblemDetail(Throwable ex) {
        return switch (ex) {
            case OficinaNoEncontradaException e -> manejarOficinaNoEncontrada(e);
            case CitaNoEncontradaException e -> manejarCitaNoEncontrada(e);
            case CupoAgotadoException e -> manejarCupoAgotado(e);
            case ObjectOptimisticLockingFailureException e -> manejarCupoAgotado(e);
            case CitaDuplicadaException e -> manejarCitaDuplicada(e);
            case ReglaReservaVioladaException e -> manejarReglaNegocio(e);
            case TransicionEstadoInvalidaException e -> manejarReglaNegocio(e);
            case IllegalArgumentException e -> manejarArgumentoInvalido(e);
            default -> {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage() != null ? ex.getMessage() : "Error interno en función serverless"
                );
                problem.setTitle("Error interno de función");
                problem.setType(URI.create(BASE_PROBLEMA_URI + "error-interno"));
                yield problem;
            }
        };
    }
}
