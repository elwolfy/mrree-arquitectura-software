package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.TipoTramite;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Comando inmutable con los datos de negocio para registrar una cita consular.
 * <p>Concepto: Objeto comando de caso de uso en arquitectura hexagonal (sección 1.3).
 * <p>Sesión: S3.
 */
public record RegistrarCitaCommand(
        String dni,
        String nombres,
        String email,
        String oficinaId,
        LocalDate fecha,
        LocalTime hora,
        TipoTramite tipoTramite
) {
}

