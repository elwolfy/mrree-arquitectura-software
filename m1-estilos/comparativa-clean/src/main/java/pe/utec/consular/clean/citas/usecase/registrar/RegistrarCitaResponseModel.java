package pe.utec.consular.clean.citas.usecase.registrar;

import pe.utec.consular.clean.citas.entities.EstadoCita;
import pe.utec.consular.clean.citas.entities.TipoTramite;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Estructura de datos de salida (Response Model) entregada por el Interactor al Output Boundary.
 * <p>Concepto: Clean Architecture – Response Model desacoplado de las entidades y de la vista HTTP (sección 1.3 del plan).
 * <p>Sesión: S2. En la versión hexagonal ({@code RegistrarCitaService}), el caso de uso devuelve directamente la entidad {@code Cita}.
 */
public record RegistrarCitaResponseModel(
        UUID id,
        String codigo,
        String dni,
        String nombres,
        String oficinaId,
        LocalDate fecha,
        LocalTime hora,
        TipoTramite tipoTramite,
        EstadoCita estado,
        String notificacion,
        Instant creadaEn
) {
}

