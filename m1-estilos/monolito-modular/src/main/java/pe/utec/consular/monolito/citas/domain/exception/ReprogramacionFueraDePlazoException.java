package pe.utec.consular.monolito.citas.domain.exception;

import pe.utec.consular.monolito.compartido.domain.exception.ReglaNegocioException;

/**
 * Excepción de dominio lanzada cuando se intenta reprogramar una cita
 * con menos de 24 horas de anticipación respecto a su fecha y hora actuales.
 * <p>Concepto: Excepción de dominio pura en arquitectura hexagonal (sección 1.3).
 * <p>Sesión: S2 (Asignación A1).
 */
public class ReprogramacionFueraDePlazoException extends ReglaNegocioException {

    public ReprogramacionFueraDePlazoException(String mensaje) {
        super(mensaje, "Reprogramación fuera de plazo", "reprogramacion-fuera-de-plazo");
    }
}
