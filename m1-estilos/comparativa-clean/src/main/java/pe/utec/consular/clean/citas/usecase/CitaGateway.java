package pe.utec.consular.clean.citas.usecase;

import pe.utec.consular.clean.citas.entities.Cita;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfaz de acceso a datos de citas requerida por los casos de uso (Entity Gateway).
 * <p>Concepto: Clean Architecture – Círculo 2 ({@code usecase}, Entity Gateway, sección 1.3 del plan).
 * Inversión de dependencias (DIP): la interfaz pertenece a {@code usecase} y es implementada
 * en {@code interfaceadapters.gateway}.
 * <p>Sesión: S2. Compárese con {@code CitaRepositoryPort} en {@code monolito-modular}.
 */
public interface CitaGateway {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(UUID id);

    List<Cita> buscarPorDni(String dni);

    boolean existeCitaActiva(String dni, String oficinaId, LocalDate fecha);

    long siguienteSecuencia();
}

