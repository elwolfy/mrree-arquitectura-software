package pe.utec.consular.citas.domain;

import pe.utec.consular.citas.domain.exception.CitaDuplicadaException;
import pe.utec.consular.citas.domain.exception.ReglaNegocioException;

import java.time.LocalDate;

/**
 * Reglas de negocio puras del dominio de reserva de citas (sección 4.2 del contrato global).
 * <p>Concepto: Invariantes de dominio sin dependencias de infraestructura (regla 01).
 * <p>Sesión: S1.
 */
public final class ReglasDeReserva {

    private static final int MAX_DIAS_ANTICIPACION = 30;

    private ReglasDeReserva() {
    }

    public static void validarVentanaTemporal(LocalDate fechaCita, LocalDate hoy) {
        if (fechaCita == null || hoy == null) {
            throw new ReglaNegocioException("La fecha de cita y la fecha actual son obligatorias");
        }
        LocalDate manana = hoy.plusDays(1);
        LocalDate fechaMaxima = hoy.plusDays(MAX_DIAS_ANTICIPACION);
        if (fechaCita.isBefore(manana) || fechaCita.isAfter(fechaMaxima)) {
            throw new ReglaNegocioException(String.format(
                    "La fecha de la cita (%s) debe encontrarse entre mañana (%s) y un máximo de %d días (%s)",
                    fechaCita, manana, MAX_DIAS_ANTICIPACION, fechaMaxima));
        }
    }

    public static void validarNoDuplicada(boolean existeCitaActiva, String dni, String oficinaId, LocalDate fecha) {
        if (existeCitaActiva) {
            throw new CitaDuplicadaException(String.format(
                    "El ciudadano con DNI %s ya cuenta con una cita reservada el %s en la oficina %s",
                    dni, fecha, oficinaId));
        }
    }
}

