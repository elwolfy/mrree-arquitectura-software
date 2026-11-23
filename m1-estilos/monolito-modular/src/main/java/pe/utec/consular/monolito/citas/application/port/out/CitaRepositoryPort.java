package pe.utec.consular.monolito.citas.application.port.out;

import pe.utec.consular.monolito.citas.domain.Cita;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario para operaciones de persistencia sobre citas.
 * <p>Concepto: Arquitectura hexagonal – puerto de salida (sección 1.3).
 * <p>Sesión: S1.
 */
public interface CitaRepositoryPort {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(UUID id);

    List<Cita> buscarPorDni(String dni);

    boolean existeCitaActiva(String dni, String oficinaId, LocalDate fecha);

    long siguienteSecuencia();
}
