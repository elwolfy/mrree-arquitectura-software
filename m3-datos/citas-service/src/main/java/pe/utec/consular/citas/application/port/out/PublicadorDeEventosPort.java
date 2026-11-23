package pe.utec.consular.citas.application.port.out;

import pe.utec.consular.citas.domain.CitaReservada;

/**
 * Puerto de salida para publicar eventos de integración hacia otros Bounded Contexts.
 * <p>Concepto: Arquitectura hexagonal – puerto secundario de mensajería y desacoplamiento por eventos (secciones 1.3 y 2.3).
 * <p>Sesión: S3.
 */
public interface PublicadorDeEventosPort {

    /**
     * Publica el evento {@code cita.reservada} cuando se confirma una reserva.
     *
     * @param evento datos publicados de la cita reservada
     */
    void publicarCitaReservada(CitaReservada evento);
}

