package pe.utec.consular.citas.domain;

/**
 * Estados del ciclo de vida de una cita consular.
 * Nota: El estado REPROGRAMADA pertenece exclusivamente a la asignación A1 (solucion/a1).
 * <p>Concepto: Enumeración de dominio y ciclo de vida (sección 1.2 del contrato global).
 * <p>Sesión: S3.
 */
public enum EstadoCita {
    RESERVADA,
    CANCELADA,
    ATENDIDA
}

