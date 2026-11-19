package pe.utec.consular.pagos.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.utec.consular.pagos.domain.exception.RecursoNoEncontradoException;
import pe.utec.consular.pagos.domain.exception.ReglaNegocioException;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Manejador centralizado de errores según RFC 9457 (Problem Details for HTTP APIs) en {@code pagos-service}.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);
    private static final String BASE_URI = "https://consular.lab/errores/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ProblemDetail pd = crearProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                detalle,
                "validacion",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler({IllegalArgumentException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ProblemDetail> manejarArgumentoInvalido(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Solicitud o cabecera requerida inválida",
                ex.getMessage(),
                "validacion",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado",
                ex.getMessage(),
                "no-encontrado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ProblemDetail> manejarReglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getTitulo(),
                ex.getMessage(),
                ex.getCodigo(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en pagos-service", ex);
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado",
                "error-interno",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    private ProblemDetail crearProblemDetail(HttpStatus status, String titulo, String detalle, String codigo, String path) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        pd.setType(URI.create(BASE_URI + codigo));
        pd.setInstance(URI.create(path));
        pd.setProperty("codigo", codigo);
        return pd;
    }
}

