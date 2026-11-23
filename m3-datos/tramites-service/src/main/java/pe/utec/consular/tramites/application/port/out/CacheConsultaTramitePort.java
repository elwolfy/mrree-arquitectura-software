package pe.utec.consular.tramites.application.port.out;

import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario para el caché de lecturas de trámites bajo la estrategia <b>Cache-Aside</b>
 * (Andamiaje de la Asignación A4).
 * <p>Conceptos: Patrón Cache-Aside, TTL e invalidación explícita al proyectar cambios de estado (sección 3.4).
 * <p>Sesión: S5.
 */
public interface CacheConsultaTramitePort {

    Optional<VistaTramite> obtener(UUID id);

    void guardar(UUID id, VistaTramite vista);

    void invalidar(UUID id);
}

