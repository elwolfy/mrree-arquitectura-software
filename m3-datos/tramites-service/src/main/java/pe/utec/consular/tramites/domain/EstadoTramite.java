package pe.utec.consular.tramites.domain;

/**
 * Estados del ciclo de vida de un trámite consular en M2 (sección 1.2 del contrato global).
 */
public enum EstadoTramite {
    REGISTRADO,
    PENDIENTE_PAGO,
    PAGADO,
    EN_EMISION,
    EMITIDO,
    OBSERVADO
}
