package pe.utec.consular.serverless.infraestructura;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.serverless.dominio.Cupo;

@Component
public class GeneradorDeCupos {

    private static final int CAPACIDAD_POR_TURNO = 5;
    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_ULTIMO_TURNO = LocalTime.of(15, 30);

    private final CupoSpringDataRepository cupoRepository;

    public GeneradorDeCupos(CupoSpringDataRepository cupoRepository) {
        this.cupoRepository = cupoRepository;
    }

    @Transactional
    public List<Cupo> obtenerOGenerarCupos(String oficinaId, LocalDate fecha) {
        List<CupoJpaEntity> existentes = cupoRepository.findByOficinaIdAndFechaOrderByHoraAsc(oficinaId, fecha);
        if (!existentes.isEmpty()) {
            return existentes.stream().map(this::aDominio).toList();
        }

        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return List.of();
        }

        List<CupoJpaEntity> nuevos = new ArrayList<>();
        LocalTime horaActual = HORA_INICIO;
        while (!horaActual.isAfter(HORA_ULTIMO_TURNO)) {
            nuevos.add(new CupoJpaEntity(
                    UUID.randomUUID(),
                    oficinaId,
                    fecha,
                    horaActual,
                    CAPACIDAD_POR_TURNO,
                    0,
                    0
            ));
            horaActual = horaActual.plusMinutes(30);
        }
        return cupoRepository.saveAll(nuevos).stream().map(this::aDominio).toList();
    }

    private Cupo aDominio(CupoJpaEntity e) {
        return new Cupo(
                e.getId(),
                e.getOficinaId(),
                e.getFecha(),
                e.getHora(),
                e.getCapacidad(),
                e.getOcupados(),
                e.getVersion()
        );
    }
}

