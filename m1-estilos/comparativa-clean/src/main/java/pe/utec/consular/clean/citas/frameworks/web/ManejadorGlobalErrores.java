package pe.utec.consular.clean.citas.frameworks.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.utec.consular.clean.citas.entities.exception.BaseDeDatosNoDisponibleException;
import pe.utec.consular.clean.citas.entities.exception.CitaDuplicadaException;
import pe.utec.consular.clean.citas.entities.exception.CupoSinCapacidadException;
import pe.utec.consular.clean.citas.entities.exception.NotificacionFallidaException;
import pe.utec.consular.clean.citas.entities.exception.RecursoNoEncontradoException;
import pe.utec.consular.clean.citas.entities.exception.ReglaNegocioException;
import pe.utec.consular.clean.tarifas.entities.TarifaNoDisponibleException;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Manejador centralizado de errores según RFC 9457 (Problem Details for HTTP APIs) en {@code frameworks.web}.
 * <p>Concepto: Clean Architecture – Círculo 4 ({@code frameworks.web}) y contrato de errores HTTP (sección 2 del contrato global).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.compartido.web.ManejadorGlobalErrores}.
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

    @ExceptionHandler({IllegalArgumentException.class, ReglaNegocioException.class})
    public ResponseEntity<ProblemDetail> manejarArgumentoInvalido(RuntimeException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Parámetro o dato de entrada inválido",
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

    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<ProblemDetail> manejarCitaDuplicada(CitaDuplicadaException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.CONFLICT,
                "Cita duplicada",
                ex.getMessage(),
                "cita-duplicada",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(CupoSinCapacidadException.class)
    public ResponseEntity<ProblemDetail> manejarCupoSinCapacidad(CupoSinCapacidadException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.CONFLICT,
                "Cupo sin capacidad",
                ex.getMessage(),
                "cupo-sin-capacidad",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(TarifaNoDisponibleException.class)
    public ResponseEntity<ProblemDetail> manejarTarifaNoDisponible(TarifaNoDisponibleException ex, HttpServletRequest request) {
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Tarifa no disponible",
                ex.getMessage(),
                "tarifa-no-disponible",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
    }

    @ExceptionHandler({BaseDeDatosNoDisponibleException.class, CannotCreateTransactionException.class})
    public ResponseEntity<ProblemDetail> manejarBaseDeDatosNoDisponible(Exception ex, HttpServletRequest request) {
        log.warn("Falla de conectividad a la base de datos (Modo CP o Timeout): {}", ex.getMessage());
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Base de datos no disponible",
                "La base de datos no responde o la conexión fue interrumpida (modo CP)",
                "base-de-datos-no-disponible",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(pd);
    }

    @ExceptionHandler(NotificacionFallidaException.class)
    public ResponseEntity<ProblemDetail> manejarNotificacionFallida(NotificacionFallidaException ex, HttpServletRequest request) {
        log.error("Falla en servicio síncrono de notificaciones: {}", ex.getMessage());
        ProblemDetail pd = crearProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error en el envío de notificación",
                ex.getMessage(),
                "notificacion-fallida",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en la aplicación", ex);
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

