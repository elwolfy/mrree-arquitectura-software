package pe.utec.consular.monolito.citas.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.DisponibilidadCachePort;
import pe.utec.consular.monolito.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cupo;
import pe.utec.consular.monolito.citas.domain.Disponibilidad;
import pe.utec.consular.monolito.citas.domain.Disponibilidad.CupoDisponibilidad;
import pe.utec.consular.monolito.citas.domain.OficinaConsular;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida para almacenamiento en memoria caché con Caffeine.
 * Implementa el refresco proactivo cada 10 segundos de los próximos 7 días por oficina.
 * <p>Concepto: Teorema CAP / Modo AP (sección 1.4 del plan y regla 6 del contrato global).
 * <p>Sesión: S1.
 */
@Component
public class DisponibilidadCaffeineAdapter implements DisponibilidadCachePort {

    private static final Logger log = LoggerFactory.getLogger(DisponibilidadCaffeineAdapter.class);

    private record EntradaCache(Disponibilidad disponibilidad, Instant guardadoEn) {
    }

    private final Cache<String, EntradaCache> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(500)
            .build();

    private final CupoRepositoryPort cupoRepositoryPort;
    private final OficinaRepositoryPort oficinaRepositoryPort;

    public DisponibilidadCaffeineAdapter(CupoRepositoryPort cupoRepositoryPort, OficinaRepositoryPort oficinaRepositoryPort) {
        this.cupoRepositoryPort = cupoRepositoryPort;
        this.oficinaRepositoryPort = oficinaRepositoryPort;
    }

    @Override
    public Optional<Disponibilidad> obtener(String oficinaId, LocalDate fecha) {
        String clave = clave(oficinaId, fecha);
        EntradaCache entrada = cache.getIfPresent(clave);
        if (entrada == null) {
            return Optional.empty();
        }

        long antiguedadSegundos = Duration.between(entrada.guardadoEn(), Instant.now()).toSeconds();
        Disponibilidad dispOriginal = entrada.disponibilidad();

        Disponibilidad resultadoCache = new Disponibilidad(
                dispOriginal.oficinaId(),
                dispOriginal.fecha(),
                dispOriginal.cupos(),
                "CACHE",
                antiguedadSegundos
        );
        return Optional.of(resultadoCache);
    }

    @Override
    public void guardar(String oficinaId, LocalDate fecha, Disponibilidad disponibilidad) {
        String clave = clave(oficinaId, fecha);
        cache.put(clave, new EntradaCache(disponibilidad, Instant.now()));
    }

    /**
     * Tarea programada: refresca cada 10 s la disponibilidad de los próximos 7 días por oficina.
     */
    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void refrescarDisponibilidadProactiva() {
        try {
            List<OficinaConsular> oficinas = oficinaRepositoryPort.listarTodas();
            if (oficinas.isEmpty()) {
                return;
            }

            LocalDate hoy = LocalDate.now();
            for (OficinaConsular oficina : oficinas) {
                for (int i = 0; i < 7; i++) {
                    LocalDate fecha = hoy.plusDays(i);
                    List<Cupo> cupos = cupoRepositoryPort.buscarPorOficinaYFecha(oficina.getId(), fecha);
                    List<CupoDisponibilidad> items = cupos.stream()
                            .sorted(Comparator.comparing(Cupo::getHora))
                            .map(c -> new CupoDisponibilidad(c.getHora(), c.getCapacidad(), c.getOcupados(), c.cuposLibres()))
                            .toList();

                    Disponibilidad disp = new Disponibilidad(oficina.getId(), fecha, items, "BASE_DE_DATOS", 0);
                    guardar(oficina.getId(), fecha, disp);
                }
            }
            log.debug("Caché Caffeine de disponibilidad refrescada para los próximos 7 días");
        } catch (Exception ex) {
            log.debug("No se pudo refrescar caché Caffeine de disponibilidad (posible partición de red): {}", ex.getMessage());
        }
    }

    private String clave(String oficinaId, LocalDate fecha) {
        return (oficinaId + "#" + fecha).toUpperCase();
    }
}
