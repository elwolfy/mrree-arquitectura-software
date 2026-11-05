package pe.utec.consular.monolito.citas.domain;

import pe.utec.consular.monolito.compartido.domain.exception.CitaDuplicadaException;
import pe.utec.consular.monolito.compartido.domain.exception.ReglaNegocioException;

import java.time.LocalDate;

/**
 * Reglas de negocio del dominio para la reserva de citas consulares.
 * Implementa las restricciones de la sección 4.2 del contrato global.
 * <p>Concepto: Domain Service / Reglas de negocio puras (sección 1.3).
 * <p>Sesión: S1.
 */
public final class ReglasDeReserva {

    private ReglasDeReserva() {
    }

    /**
     * Valida que la fecha solicitada esté comprendida entre mañana y los próximos 30 días.
     *
     * @param fecha fecha deseada para la cita
     * @param hoy fecha de referencia (normalmente LocalDate.now())
     */
    public static void validarVentanaTemporal(LocalDate fecha, LocalDate hoy) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la cita no puede ser nula");
        }
        LocalDate fechaMinima = hoy.plusDays(1);
        LocalDate fechaMaxima = hoy.plusDays(30);

        if (fecha.isBefore(fechaMinima)) {
            throw new ReglaNegocioException(String.format(
                    "La fecha de la cita (%s) debe ser a partir de mañana (%s)", fecha, fechaMinima));
        }
        if (fecha.isAfter(fechaMaxima)) {
            throw new ReglaNegocioException(String.format(
                    "La fecha de la cita (%s) no puede exceder el límite de 30 días (%s)", fecha, fechaMaxima));
        }
    }

    /**
     * Valida que no exista una cita activa para el mismo ciudadano en la misma fecha y oficina.
     *
     * @param existeDuplicado indicador si ya se encontró una cita previa RESERVADA
     * @param dni DNI del ciudadano
     * @param oficinaId identificador de la oficina
     * @param fecha fecha solicitada
     */
    public static void validarNoDuplicada(boolean existeDuplicado, String dni, String oficinaId, LocalDate fecha) {
        if (existeDuplicado) {
            throw new CitaDuplicadaException(String.format(
                    "El ciudadano con DNI %s ya cuenta con una cita reservada el %s en la oficina %s",
                    dni, fecha, oficinaId));
        }
    }
}
