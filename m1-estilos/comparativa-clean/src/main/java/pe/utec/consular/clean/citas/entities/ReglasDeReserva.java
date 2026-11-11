package pe.utec.consular.clean.citas.entities;

import pe.utec.consular.clean.citas.entities.exception.CitaDuplicadaException;
import pe.utec.consular.clean.citas.entities.exception.ReglaNegocioException;

import java.time.LocalDate;

/**
 * Reglas empresariales puras para la reserva de citas consulares (sección 4.2 del contrato global).
 * <p>Concepto: Clean Architecture – Círculo 1 ({@code entities}, Reglas Empresariales, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.domain.ReglasDeReserva}.
 */
public final class ReglasDeReserva {

    private ReglasDeReserva() {
    }

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

    public static void validarNoDuplicada(boolean existeDuplicado, String dni, String oficinaId, LocalDate fecha) {
        if (existeDuplicado) {
            throw new CitaDuplicadaException(String.format(
                    "El ciudadano con DNI %s ya cuenta con una cita reservada el %s en la oficina %s",
                    dni, fecha, oficinaId));
        }
    }
}

