package pe.utec.consular.clean.tarifas.entities;

/**
 * Excepción lanzada cuando se solicita calcular la tarifa de un trámite sin estrategia disponible (HTTP 422).
 */
public class TarifaNoDisponibleException extends RuntimeException {

    public TarifaNoDisponibleException(String message) {
        super(message);
    }
}

