package pe.utec.consular.clean.citas.interfaceadapters.gateway;

import pe.utec.consular.clean.citas.entities.Cita;
import pe.utec.consular.clean.citas.usecase.CitaGateway;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del gateway de citas en el círculo de adaptadores de interfaz ({@code interfaceadapters.gateway}).
 * <p>Concepto: Clean Architecture – Interface Adapter Gateway (Círculo 3, sección 1.3 del plan).
 * Implementa {@link CitaGateway} (definida en {@code usecase}) delegando el almacenamiento físico
 * en {@link CitaPersistenceDataSource} (implementado en {@code frameworks.persistence}).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.adapter.out.persistence.CitaJpaAdapter}.
 */
public class CitaJpaGateway implements CitaGateway {

    private final CitaPersistenceDataSource dataSource;

    public CitaJpaGateway(CitaPersistenceDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Cita guardar(Cita cita) {
        return dataSource.guardar(cita);
    }

    @Override
    public Optional<Cita> buscarPorId(UUID id) {
        return dataSource.buscarPorId(id);
    }

    @Override
    public List<Cita> buscarPorDni(String dni) {
        return dataSource.buscarPorDni(dni);
    }

    @Override
    public boolean existeCitaActiva(String dni, String oficinaId, LocalDate fecha) {
        return dataSource.existeCitaActiva(dni, oficinaId, fecha);
    }

    @Override
    public long siguienteSecuencia() {
        return dataSource.siguienteSecuencia();
    }
}

