package pe.utec.consular.citas.application.port.out;

import pe.utec.consular.citas.domain.Cupo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la gestión de cupos y bloqueo pesimista de capacidad.
 * <p>Concepto: Arquitectura hexagonal – puerto secundario (sección 1.3).
 * <p>Sesión: S3.
 */
public interface CupoRepositoryPort {

    Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora);

    List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha);

    List<Cupo> buscarPorOficinaYFechaEntre(String oficinaId, LocalDate desde, LocalDate hasta);

    Cupo guardar(Cupo cupo);

    void guardarTodos(List<Cupo> cupos);

    boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha);
}

