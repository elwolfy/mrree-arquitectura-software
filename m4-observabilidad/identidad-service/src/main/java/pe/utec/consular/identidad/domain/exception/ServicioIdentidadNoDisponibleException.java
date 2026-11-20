package pe.utec.consular.identidad.domain.exception;

/**
 * Excepción lanzada cuando el proveedor externo de identidad no responde dentro del umbral
 * o el Circuit Breaker se encuentra abierto.
 * Se traduce a HTTP 503 Service Unavailable ("Servicio de identidad no disponible").
 */
public class ServicioIdentidadNoDisponibleException extends RuntimeException {

    public ServicioIdentidadNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

