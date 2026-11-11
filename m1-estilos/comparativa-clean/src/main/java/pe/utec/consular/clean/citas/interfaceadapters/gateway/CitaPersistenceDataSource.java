package pe.utec.consular.clean.citas.interfaceadapters.gateway;

import pe.utec.consular.clean.citas.entities.Cita;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia que el círculo {@code frameworks.persistence} implementa para {@link CitaJpaGateway}.
 * Garantiza que {@code interfaceadapters} no dependa hacia afuera de {@code frameworks}.
 * <p>Concepto: Regla de Dependencia Concéntrica en Clean Architecture (sección 1.3 del plan).
 */
public interface CitaPersistenceDataSource {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(UUID id);

    List<Cita> buscarPorDni(String dni);

    boolean existeCitaActiva(String dni, String oficinaId, LocalDate fecha);

    long siguienteSecuencia();
}

