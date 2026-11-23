package pe.utec.consular.tramites.application.port.out;

import pe.utec.consular.tramites.domain.TramiteRegistrado;

/**
 * Puerto de salida para publicar eventos de integración hacia otros Bounded Contexts.
 * <p>Concepto: Arquitectura hexagonal – puerto secundario de mensajería con implementación propia
 * sin compartir código con otros microservicios (regla 04 y sección 6.1 del contrato).
 * <p>Sesión: S3 y S5.
 */
public interface PublicadorDeEventosPort {

    /**
     * Publica el evento {@code tramite.registrado} cuando se inicia un nuevo trámite consular.
     *
     * @param evento carga útil del trámite registrado
     */
    void publicarTramiteRegistrado(TramiteRegistrado evento);
}
