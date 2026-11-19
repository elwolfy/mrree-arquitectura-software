package pe.utec.consular.tramites.domain.exception;

/**
 * Excepción de dominio lanzada cuando se intenta aplicar una operación o evento sobre un {@code Tramite}
 * cuyo estado actual no admite la transición solicitada.
 * <p>Concepto: Protección de invariantes de máquina de estados dentro del agregado con Event Sourcing.
 * <p>Sesión: S4 y S5.
 */
public class TransicionEstadoInvalidaException extends ReglaNegocioException {

    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje, "Transición de estado de trámite inválida", "transicion-estado-invalida");
    }
}

