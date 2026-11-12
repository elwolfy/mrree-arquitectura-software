package pe.utec.consular.citas.domain;

/**
 * Estados del ciclo de vida de una cita consular (sección 1.2 del contrato global).
 */
public enum EstadoCita {
    RESERVADA,
    REPROGRAMADA,
    CANCELADA,
    ATENDIDA
}

