package pe.utec.consular.citas.application.port.out;

import pe.utec.consular.citas.domain.Cita;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de citas.
 */
public interface CitaRepositoryPort {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(UUID id);

    List<Cita> buscarPorDni(String dni);

    boolean existeCitaActiva(String dni, String oficinaId, LocalDate fecha);

    long siguienteSecuencia();
}

