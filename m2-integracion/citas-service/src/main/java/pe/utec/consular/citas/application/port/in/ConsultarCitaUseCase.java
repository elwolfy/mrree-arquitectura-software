package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.Cita;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para consultar citas por identificador o DNI.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3).
 * <p>Sesión: S3.
 */
public interface ConsultarCitaUseCase {

    Optional<Cita> consultarPorId(UUID id);

    List<Cita> consultarPorDni(String dni);
}

