package pe.utec.consular.monolito.citas.application.port.in;

import pe.utec.consular.monolito.citas.domain.Cita;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada primario para consultar citas consulares por identificador o DNI.
 * <p>Concepto: Arquitectura hexagonal – puerto de entrada (sección 1.3).
 */
public interface ConsultarCitaUseCase {

    Optional<Cita> consultarPorId(UUID id);

    List<Cita> consultarPorDni(String dni);
}
