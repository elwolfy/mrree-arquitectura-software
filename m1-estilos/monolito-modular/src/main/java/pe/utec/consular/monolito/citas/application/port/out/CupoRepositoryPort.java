package pe.utec.consular.monolito.citas.application.port.out;

import pe.utec.consular.monolito.citas.domain.Cupo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto secundario para acceso y gestión concurrente de cupos consulares.
 * <p>Concepto: Bloqueo pesimista para evitar sobreasignación (sección 4.2 del contrato global).
 * <p>Sesión: S1.
 */
public interface CupoRepositoryPort {

    Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora);

    List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha);

    List<Cupo> buscarPorOficinaYFechaEntre(String oficinaId, LocalDate desde, LocalDate hasta);

    Cupo guardar(Cupo cupo);

    void guardarTodos(List<Cupo> cupos);

    boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha);
}
