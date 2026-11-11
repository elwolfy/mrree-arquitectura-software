package pe.utec.consular.citas.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada para cancelar una cita existente.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3).
 * <p>Sesión: S3.
 */
public interface CancelarCitaUseCase {

    void cancelar(UUID id);
}

