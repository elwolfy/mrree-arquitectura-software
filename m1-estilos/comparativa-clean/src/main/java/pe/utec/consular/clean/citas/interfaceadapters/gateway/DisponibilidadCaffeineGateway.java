package pe.utec.consular.clean.citas.interfaceadapters.gateway;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.utec.consular.clean.citas.entities.Cupo;
import pe.utec.consular.clean.citas.entities.Disponibilidad;
import pe.utec.consular.clean.citas.entities.Disponibilidad.CupoDisponibilidad;
import pe.utec.consular.clean.citas.entities.OficinaConsular;
import pe.utec.consular.clean.citas.usecase.CupoGateway;
import pe.utec.consular.clean.citas.usecase.DisponibilidadCacheGateway;
import pe.utec.consular.clean.citas.usecase.OficinaGateway;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Gateway de almacenamiento en caché Caffeine para disponibilidad (modo AP del Teorema CAP).
 * <p>Concepto: Clean Architecture – Interface Adapter Gateway y Teorema CAP (secciones 1.3 y 1.4 del plan).
 * <p>Sesión: S2. Compárese con {@code DisponibilidadCaffeineAdapter} en {@code monolito-modular}.
 */
public class DisponibilidadCaffeineGateway implements DisponibilidadCacheGateway {

    private static final Logger log = LoggerFactory.getLogger(DisponibilidadCaffeineGateway.class);

    private record EntradaCache(Disponibilidad disponibilidad, Instant guardadoEn) {
    }

    private final Cache<String, EntradaCache> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(500)
            .build();

    private final CupoGateway cupoGateway;
    private final OficinaGateway oficinaGateway;

    public DisponibilidadCaffeineGateway(CupoGateway cupoGateway, OficinaGateway oficinaGateway) {
        this.cupoGateway = cupoGateway;
        this.oficinaGateway = oficinaGateway;
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

        return Optional.of(new Disponibilidad(
                dispOriginal.oficinaId(),
                dispOriginal.fecha(),
                dispOriginal.cupos(),
                "CACHE",
                antiguedadSegundos
        ));
    }

    @Override
    public void guardar(String oficinaId, LocalDate fecha, Disponibilidad disponibilidad) {
        cache.put(clave(oficinaId, fecha), new EntradaCache(disponibilidad, Instant.now()));
    }

    public void refrescarDisponibilidadProactiva() {
        try {
            List<OficinaConsular> oficinas = oficinaGateway.listarTodas();
            if (oficinas.isEmpty()) {
                return;
            }
            LocalDate hoy = LocalDate.now();
            for (OficinaConsular oficina : oficinas) {
                for (int i = 0; i < 7; i++) {
                    LocalDate fecha = hoy.plusDays(i);
                    List<Cupo> cupos = cupoGateway.buscarPorOficinaYFecha(oficina.getId(), fecha);
                    List<CupoDisponibilidad> items = cupos.stream()
                            .sorted(Comparator.comparing(Cupo::getHora))
                            .map(c -> new CupoDisponibilidad(c.getHora(), c.getCapacidad(), c.getOcupados(), c.cuposLibres()))
                            .toList();
                    guardar(oficina.getId(), fecha, new Disponibilidad(oficina.getId(), fecha, items, "BASE_DE_DATOS", 0));
                }
            }
        } catch (Exception ex) {
            log.debug("No se pudo refrescar caché Caffeine de disponibilidad: {}", ex.getMessage());
        }
    }

    private String clave(String oficinaId, LocalDate fecha) {
        return (oficinaId + "#" + fecha).toUpperCase();
    }
}

