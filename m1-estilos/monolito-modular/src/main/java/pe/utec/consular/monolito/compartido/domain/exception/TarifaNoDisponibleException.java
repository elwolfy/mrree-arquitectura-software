package pe.utec.consular.monolito.compartido.domain.exception;

/**
 * Lanzada cuando no existe una estrategia tarifaria que soporte el trámite solicitado.
 * Mapeada al código RFC 9457: "tarifa-no-disponible" (HTTP 422 Unprocessable Entity).
 * <p>Concepto: Patrón Strategy y Principio Abierto/Cerrado (sección 2.1 del plan).
 */
public class TarifaNoDisponibleException extends ReglaNegocioException {
    public TarifaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
