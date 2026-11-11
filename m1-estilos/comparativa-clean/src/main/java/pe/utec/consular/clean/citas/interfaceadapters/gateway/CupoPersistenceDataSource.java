package pe.utec.consular.clean.citas.interfaceadapters.gateway;

import pe.utec.consular.clean.citas.entities.Cupo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de cupos que {@code frameworks.persistence} implementa para {@link CupoJpaGateway}.
 */
public interface CupoPersistenceDataSource {

    Optional<Cupo> buscarPorOficinaFechaHoraConBloqueo(String oficinaId, LocalDate fecha, LocalTime hora);

    List<Cupo> buscarPorOficinaYFecha(String oficinaId, LocalDate fecha);

    boolean existenCuposParaOficinaYFecha(String oficinaId, LocalDate fecha);

    Cupo guardar(Cupo cupo);

    void guardarTodos(List<Cupo> cupos);
}

