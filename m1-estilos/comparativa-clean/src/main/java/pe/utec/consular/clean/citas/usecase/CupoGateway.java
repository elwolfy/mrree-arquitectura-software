package pe.utec.consular.clean.citas.usecase;

import pe.utec.consular.clean.citas.entities.Cupo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a cupos horarios requerida por los casos de uso (Entity Gateway).
 * <p>Concepto: Clean Architecture – Círculo 2 ({@code usecase}, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code CupoRepositoryPort} en {@code monolito-modular}.
 */
public interface CupoGateway {

    Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora);

    List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha);

    boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha);

    Cupo guardar(Cupo cupo);

    void guardarTodos(List<Cupo> cupos);
}

