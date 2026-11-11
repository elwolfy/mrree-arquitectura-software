package pe.utec.consular.clean.citas.usecase;

import pe.utec.consular.clean.citas.entities.Disponibilidad;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Interfaz de acceso a caché de disponibilidad para soporte del modo AP (Teorema CAP).
 * <p>Concepto: Clean Architecture – Círculo 2 ({@code usecase}, secciones 1.3 y 1.4 del plan).
 * <p>Sesión: S2. Compárese con {@code DisponibilidadCachePort} en {@code monolito-modular}.
 */
public interface DisponibilidadCacheGateway {

    Optional<Disponibilidad> obtener(String oficinaId, LocalDate fecha);

    void guardar(String oficinaId, LocalDate fecha, Disponibilidad disponibilidad);
}

