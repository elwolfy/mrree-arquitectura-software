package pe.utec.consular.tramites.domain;

/**
 * Estados del ciclo de vida de un trámite consular en M2 (sección 1.2 del contrato global).
 * <p>En M2 el flujo inicial transita de {@code REGISTRADO} a {@code PENDIENTE_PAGO}
 * (la confirmación de pago y emisión por saga llegan en M3; el estado {@code OBSERVADO}
 * se agrega en el prompt 08-7 como andamiaje de la asignación A2).
 */
public enum EstadoTramite {
    REGISTRADO,
    PENDIENTE_PAGO,
    PAGADO,
    EN_EMISION,
    EMITIDO
}

