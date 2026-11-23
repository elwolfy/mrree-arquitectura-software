package pe.utec.consular.monolito.notificaciones.application.port.out;

import pe.utec.consular.monolito.notificaciones.domain.Aviso;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de avisos.
 * <p>Concepto: Arquitectura hexagonal – puerto de salida (sección 1.3 del plan).
 * <p>Sesión: S1.
 */
public interface AvisoRepositoryPort {

    void guardar(Aviso aviso);

    Optional<Aviso> buscarPorId(UUID id);
}
