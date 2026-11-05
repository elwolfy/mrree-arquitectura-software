package pe.utec.consular.monolito.notificaciones;

/**
 * API pública del módulo de notificaciones para comunicación inter-módulos.
 * <p>Concepto: Modularidad con Spring Modulith y contrato público de módulo.
 * <p>Sesión: S1.
 */
public interface NotificacionesApi {

    /**
     * Envía un aviso al destinatario indicado.
     *
     * @param comando datos del aviso a remitir
     */
    void enviar(AvisoCommand comando);
}
