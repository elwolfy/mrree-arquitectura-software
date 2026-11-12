package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.Cita;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada para consultar citas por identificador o DNI.
 */
public interface ConsultarCitaUseCase {

    Cita buscarPorId(UUID citaId);

    List<Cita> buscarPorDni(String dni);
}

