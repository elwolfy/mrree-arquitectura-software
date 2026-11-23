package pe.utec.consular.monolito.citas.application.port.out;

import pe.utec.consular.monolito.citas.domain.Disponibilidad;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Puerto secundario para acceso y actualización de la memoria caché de disponibilidad (Caffeine).
 * <p>Concepto: Teorema CAP / PACELC (sección 1.4 del plan).
 * Permite que en modo AP la disponibilidad se sirva localmente aunque la base de datos no responda.
 */
public interface DisponibilidadCachePort {

    Optional<Disponibilidad> obtener(String oficinaId, LocalDate fecha);

    void guardar(String oficinaId, LocalDate fecha, Disponibilidad disponibilidad);
}
