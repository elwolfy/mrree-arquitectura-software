package pe.utec.consular.monolito.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.monolito.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.OficinaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cupo;
import pe.utec.consular.monolito.citas.domain.OficinaConsular;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generador idempotente de cupos iniciales al arrancar la aplicación.
 * Genera cupos para los próximos 30 días hábiles, de 08:00 a 12:30 cada 30 minutos, capacidad 2.
 * <p>Concepto: Inicialización idempotente y datos semilla (sección 5 del contrato global).
 * <p>Sesión: S1.
 */
@Component
public class GeneradorDeCupos {

    private static final Logger log = LoggerFactory.getLogger(GeneradorDeCupos.class);

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

    private final CupoRepositoryPort cupoRepositoryPort;
    private final OficinaRepositoryPort oficinaRepositoryPort;

    public GeneradorDeCupos(CupoRepositoryPort cupoRepositoryPort, OficinaRepositoryPort oficinaRepositoryPort) {
        this.cupoRepositoryPort = cupoRepositoryPort;
        this.oficinaRepositoryPort = oficinaRepositoryPort;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void alIniciar() {
        generarCuposSiNoExisten(LocalDate.now());
    }

    /**
     * Genera cupos idempotentemente para los próximos 30 días hábiles a partir de la fecha de referencia.
     */
    public void generarCuposSiNoExisten(LocalDate hoy) {
        List<OficinaConsular> oficinas = oficinaRepositoryPort.listarTodas();
        if (oficinas.isEmpty()) {
            log.warn("No se encontraron oficinas consulares registradas para generar cupos");
            return;
        }

        List<LocalDate> diasHabiles = calcularProximosDiasHabiles(hoy, DIAS_HABILES_OBJETIVO);
        int totalGenerados = 0;

        for (OficinaConsular oficina : oficinas) {
            List<Cupo> cuposParaInsertar = new ArrayList<>();
            for (LocalDate fecha : diasHabiles) {
                if (!cupoRepositoryPort.existenCuposParaOficinaYFecha(oficina.getId(), fecha)) {
                    for (LocalTime hora : HORAS_ATENCION) {
                        cuposParaInsertar.add(Cupo.nuevo(oficina.getId(), fecha, hora, CAPACIDAD_CUPO));
                    }
                }
            }
            if (!cuposParaInsertar.isEmpty()) {
                cupoRepositoryPort.guardarTodos(cuposParaInsertar);
                totalGenerados += cuposParaInsertar.size();
            }
        }

        if (totalGenerados > 0) {
            log.info("Generación inicial de cupos completada: {} cupos creados en {} oficinas para {} días hábiles",
                    totalGenerados, oficinas.size(), diasHabiles.size());
        } else {
            log.info("Cupos ya inicializados para las oficinas consulares; omitiendo generación.");
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
