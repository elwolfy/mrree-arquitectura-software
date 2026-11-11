package pe.utec.consular.clean.citas.frameworks.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.clean.citas.entities.Cupo;
import pe.utec.consular.clean.citas.entities.OficinaConsular;
import pe.utec.consular.clean.citas.interfaceadapters.gateway.DisponibilidadCaffeineGateway;
import pe.utec.consular.clean.citas.usecase.CupoGateway;
import pe.utec.consular.clean.citas.usecase.OficinaGateway;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inicializador de cupos (30 días hábiles, 08:00 a 12:30 cada 30 min, capacidad 2) y refresco de caché
 * en el círculo {@code frameworks.config}.
 * <p>Concepto: Inicialización idempotente y refresco proactivo de caché (sección 5 del contrato global).
 */
@Component
public class GeneradorDeCuposInitializer {

    private static final Logger log = LoggerFactory.getLogger(GeneradorDeCuposInitializer.class);

    private static final int DIAS_HABILES_OBJETIVO = 30;
    private static final int CAPACIDAD_CUPO = 2;
    private static final List<LocalTime> HORAS_ATENCION = List.of(
            LocalTime.of(8, 0),
            LocalTime.of(8, 30),
            LocalTime.of(9, 0),
            LocalTime.of(9, 30),
            LocalTime.of(10, 0),
            LocalTime.of(10, 30),
            LocalTime.of(11, 0),
            LocalTime.of(11, 30),
            LocalTime.of(12, 0),
            LocalTime.of(12, 30)
    );

    private final CupoGateway cupoGateway;
    private final OficinaGateway oficinaGateway;
    private final DisponibilidadCaffeineGateway disponibilidadCaffeineGateway;

    public GeneradorDeCuposInitializer(CupoGateway cupoGateway,
                                       OficinaGateway oficinaGateway,
                                       DisponibilidadCaffeineGateway disponibilidadCaffeineGateway) {
        this.cupoGateway = cupoGateway;
        this.oficinaGateway = oficinaGateway;
        this.disponibilidadCaffeineGateway = disponibilidadCaffeineGateway;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void alIniciar() {
        generarCuposSiNoExisten(LocalDate.now());
    }

    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void refrescarCacheDisponibilidad() {
        disponibilidadCaffeineGateway.refrescarDisponibilidadProactiva();
    }

    public void generarCuposSiNoExisten(LocalDate hoy) {
        List<OficinaConsular> oficinas = oficinaGateway.listarTodas();
        if (oficinas.isEmpty()) {
            log.warn("No se encontraron oficinas consulares registradas para generar cupos");
            return;
        }

        List<LocalDate> diasHabiles = calcularProximosDiasHabiles(hoy, DIAS_HABILES_OBJETIVO);
        int totalGenerados = 0;

        for (OficinaConsular oficina : oficinas) {
            List<Cupo> cuposParaInsertar = new ArrayList<>();
            for (LocalDate fecha : diasHabiles) {
                if (!cupoGateway.existenCuposParaOficinaYFecha(oficina.getId(), fecha)) {
                    for (LocalTime hora : HORAS_ATENCION) {
                        cuposParaInsertar.add(Cupo.nuevo(oficina.getId(), fecha, hora, CAPACIDAD_CUPO));
                    }
                }
            }
            if (!cuposParaInsertar.isEmpty()) {
                cupoGateway.guardarTodos(cuposParaInsertar);
                totalGenerados += cuposParaInsertar.size();
            }
        }

        if (totalGenerados > 0) {
            log.info("Generación inicial de cupos completada (Clean): {} cupos creados en {} oficinas",
                    totalGenerados, oficinas.size());
        }
    }

    private List<LocalDate> calcularProximosDiasHabiles(LocalDate desde, int cantidad) {
        List<LocalDate> dias = new ArrayList<>(cantidad);
        LocalDate actual = desde.plusDays(1);
        while (dias.size() < cantidad) {
            DayOfWeek dow = actual.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                dias.add(actual);
            }
            actual = actual.plusDays(1);
        }
        return dias;
    }
}

