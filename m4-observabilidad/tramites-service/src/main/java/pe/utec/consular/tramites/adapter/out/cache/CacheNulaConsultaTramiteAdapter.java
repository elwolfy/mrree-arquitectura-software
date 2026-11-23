package pe.utec.consular.tramites.adapter.out.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.out.CacheConsultaTramitePort;
import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador por defecto (Null Object Pattern) para {@link CacheConsultaTramitePort},
 * activo cuando {@code cache.consulta-tramite.tipo=nula} (comportamiento en {@code main}).
 * <p>En la <b>Asignación A4</b> se implementa {@code RedisCacheConsultaTramiteAdapter}
 * activado con {@code cache.consulta-tramite.tipo=redis} y TTL de 60 segundos.
 */
@Component
@ConditionalOnProperty(name = "cache.consulta-tramite.tipo", havingValue = "nula", matchIfMissing = true)
public class CacheNulaConsultaTramiteAdapter implements CacheConsultaTramitePort {

    @Override
    public Optional<VistaTramite> obtener(UUID id) {
        return Optional.empty();
    }

    @Override
    public void guardar(UUID id, VistaTramite vista) {
        // Sin operación en el adaptador de caché nula
    }

    @Override
    public void invalidar(UUID id) {
        // Sin operación en el adaptador de caché nula
    }
}

