package pe.utec.consular.identidad.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.utec.consular.identidad.domain.exception.DniNoEncontradoException;
import pe.utec.consular.identidad.domain.exception.IdentidadNoVigenteException;
import pe.utec.consular.identidad.domain.exception.ServicioIdentidadNoDisponibleException;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Manejador centralizado de errores según RFC 9457 (Problem Details for HTTP APIs).
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> manejarArgumentoInvalido(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Parámetro o dato de entrada inválido",
                ex.getMessage(),
                "validacion",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(DniNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> manejarDniNoEncontrado(DniNoEncontradoException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.NOT_FOUND,
                "DNI no encontrado",
                ex.getMessage(),
                "dni-no-encontrado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(IdentidadNoVigenteException.class)
    public ResponseEntity<ProblemDetail> manejarIdentidadNoVigente(IdentidadNoVigenteException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Identidad no vigente",
                ex.getMessage(),
                "identidad-no-vigente",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
    }

    @ExceptionHandler(ServicioIdentidadNoDisponibleException.class)
    public ResponseEntity<ProblemDetail> manejarServicioNoDisponible(ServicioIdentidadNoDisponibleException ex, HttpServletRequest request) {
        log.warn("Proveedor externo de identidad no disponible: {}", ex.getMessage());
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Servicio de identidad no disponible",
                ex.getMessage(),
                "servicio-identidad-no-disponible",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en identidad-service", ex);
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

