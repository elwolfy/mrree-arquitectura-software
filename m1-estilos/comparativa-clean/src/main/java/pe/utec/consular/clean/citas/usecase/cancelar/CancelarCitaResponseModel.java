package pe.utec.consular.clean.citas.usecase.cancelar;

import java.util.UUID;

/**
 * Modelo de respuesta al cancelar una cita consular.
 * <p>Concepto: Clean Architecture – Response Model (sección 1.3 del plan).
 */
public record CancelarCitaResponseModel(
        UUID citaId,
        String codigo,
        String estado
) {
}

