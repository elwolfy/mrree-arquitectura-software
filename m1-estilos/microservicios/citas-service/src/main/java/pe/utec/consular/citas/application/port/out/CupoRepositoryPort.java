package pe.utec.consular.citas.application.port.out;

import pe.utec.consular.citas.domain.Cupo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de cupos horarios.
 */
public interface CupoRepositoryPort {

    Optional<Cupo> buscarPorId(UUID id);

    Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora);

    List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha);

    boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha);

    Cupo guardar(Cupo cupo);

    void guardarTodos(List<Cupo> cupos);
}

