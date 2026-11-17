package pe.utec.consular.monolito.citas.domain;

import pe.utec.consular.monolito.citas.domain.exception.ReprogramacionFueraDePlazoException;

import java.time.LocalDateTime;

/**
 * Regla de negocio pura del dominio para validar el plazo mínimo de reprogramación de una cita.
 * <p>Concepto: Arquitectura hexagonal – el dominio no depende de Spring ni de infraestructura (sección 1.3).
 * <p>Sesión: S2 (Asignación A1 – Tarea 2).
 */
public final class ReglaDeReprogramacion {

    private ReglaDeReprogramacion() {
    }

    /**
     * Valida que falten al menos 24 horas entre el instante actual ({@code ahora})
     * y la fecha y hora programadas en la {@code cita} actual.
     *
     * @param cita  cita existente que se desea reprogramar
     * @param ahora fecha y hora de referencia (en zona America/Lima)
     * @throws ReprogramacionFueraDePlazoException si faltan menos de 24 horas para la cita actual
     */
    public static void validar(Cita cita, LocalDateTime ahora) {
        // TODO Asignación A1 – Tarea 2: lanzar ReprogramacionFueraDePlazoException
        // si faltan menos de 24 horas para la fecha y hora actuales de la cita.
        throw new UnsupportedOperationException("Asignación A1 pendiente");
    }
}

