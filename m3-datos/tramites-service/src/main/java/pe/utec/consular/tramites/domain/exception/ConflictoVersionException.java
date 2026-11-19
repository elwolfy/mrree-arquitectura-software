package pe.utec.consular.tramites.domain.exception;

/**
 * Excepción de dominio lanzada cuando se detecta un conflicto de concurrencia optimista
 * al intentar persistir o reconstruir eventos de un {@code Tramite} con una versión
 * que no coincide con la secuencia esperada en {@code tramites.eventos_tramite}.
 * <p>Concepto: Control de concurrencia optimista en Event Sourcing mediante número de versión
 * monótono por agregado ({@code UNIQUE(tramite_id, version)}).
 * <p>Sesión: S4 y S5.
 */
public class ConflictoVersionException extends ReglaNegocioException {

    public ConflictoVersionException(String mensaje) {
        super(mensaje, "Conflicto de concurrencia optimista en el flujo de eventos", "conflicto-version");
    }
}

