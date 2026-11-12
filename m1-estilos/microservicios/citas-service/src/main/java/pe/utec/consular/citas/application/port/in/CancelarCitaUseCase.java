package pe.utec.consular.citas.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada para cancelar una cita activa.
 */
public interface CancelarCitaUseCase {

    void cancelar(UUID citaId);
}

