package pe.utec.consular.monolito.citas.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada primario para cancelar una cita consular existente.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3).
 */
public interface CancelarCitaUseCase {

    void cancelar(UUID id);
}
