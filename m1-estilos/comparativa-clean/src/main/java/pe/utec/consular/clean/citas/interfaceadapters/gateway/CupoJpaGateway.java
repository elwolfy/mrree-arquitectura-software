package pe.utec.consular.clean.citas.interfaceadapters.gateway;

import pe.utec.consular.clean.citas.entities.Cupo;
import pe.utec.consular.clean.citas.usecase.CupoGateway;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del gateway de cupos en {@code interfaceadapters.gateway}.
 * <p>Concepto: Clean Architecture – Interface Adapter Gateway (Círculo 3, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.adapter.out.persistence.CupoJpaAdapter}.
 */
public class CupoJpaGateway implements CupoGateway {

    private final CupoPersistenceDataSource dataSource;

    public CupoJpaGateway(CupoPersistenceDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora) {
        return dataSource.buscarPorOficinaFechaHoraConBloqueo(oficinaId, fecha, hora);
    }

    @Override
    public List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha) {
        return dataSource.buscarPorOficinaYFecha(oficinaId, fecha);
    }

    @Override
    public boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha) {
        return dataSource.existenCuposParaOficinaYFecha(oficinaId, fecha);
    }

    @Override
    public Cupo guardar(Cupo cupo) {
        return dataSource.guardar(cupo);
    }

    @Override
    public void guardarTodos(List<Cupo> cupos) {
        dataSource.guardarTodos(cupos);
    }
}

