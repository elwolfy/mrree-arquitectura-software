package pe.utec.consular.notificaciones.application.port.in;

import pe.utec.consular.notificaciones.domain.Aviso;

/**
 * Puerto de entrada para enviar un aviso al ciudadano.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3 del plan).
 * <p>Sesión: S1 y S2.
 */
public interface EnviarAvisoUseCase {

    Aviso enviar(EnviarAvisoCommand comando);
}

