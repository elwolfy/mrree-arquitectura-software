package pe.utec.consular.tramites.domain;

/**
 * Estados del ciclo de vida de un trámite consular en M3 (secciones 1.2, 6.2 y 8 del contrato global).
 */
public enum EstadoTramite {
    REGISTRADO,
    PENDIENTE_PAGO,
    PAGADO,
    EN_EMISION,
    EMITIDO,
    RECHAZADO,
    OBSERVADO
}
