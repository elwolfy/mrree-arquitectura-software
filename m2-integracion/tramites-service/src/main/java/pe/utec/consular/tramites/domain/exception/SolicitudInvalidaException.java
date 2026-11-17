package pe.utec.consular.tramites.domain.exception;

/**
 * Excepción de dominio lanzada cuando la construcción de una {@code Solicitud}
 * o la creación de un {@code Tramite} incumple datos obligatorios o reglas de coherencia por tipo.
 */
public class SolicitudInvalidaException extends ReglaNegocioException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje, "Solicitud de trámite inválida", "solicitud-invalida");
    }
}

