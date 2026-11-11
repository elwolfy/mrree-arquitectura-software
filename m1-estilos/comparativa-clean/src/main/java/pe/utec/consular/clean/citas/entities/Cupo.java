package pe.utec.consular.clean.citas.entities;

import pe.utec.consular.clean.citas.entities.exception.CupoSinCapacidadException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad empresarial que gestiona la capacidad y concurrencia de una franja horaria.
 * <p>Concepto: Clean Architecture – Círculo 1 ({@code entities}, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.domain.Cupo}.
 */
public class Cupo {

    private final UUID id;
    private final String oficinaId;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final int capacidad;
    private int ocupados;
    private int version;

    public Cupo(UUID id, String oficinaId, LocalDate fecha, LocalTime hora, int capacidad, int ocupados, int version) {
        this.id = Objects.requireNonNull(id, "El identificador del cupo no puede ser nulo");
        this.oficinaId = Objects.requireNonNull(oficinaId, "El identificador de oficina no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.hora = Objects.requireNonNull(hora, "La hora no puede ser nula");
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad del cupo debe ser mayor a cero");
        }
        if (ocupados < 0) {
            throw new IllegalArgumentException("La cantidad de ocupados no puede ser negativa");
        }
        this.capacidad = capacidad;
        this.ocupados = ocupados;
        this.version = version;
    }

    public static Cupo nuevo(String oficinaId, LocalDate fecha, LocalTime hora, int capacidad) {
        return new Cupo(UUID.randomUUID(), oficinaId, fecha, hora, capacidad, 0, 0);
    }

    public boolean tieneCapacidadLibre() {
        return ocupados < capacidad;
    }

    public int cuposLibres() {
        return Math.max(0, capacidad - ocupados);
    }

    public void reservar() {
        if (!tieneCapacidadLibre()) {
            throw new CupoSinCapacidadException(String.format(
                    "El cupo para la oficina %s en la fecha %s a las %s ya no tiene capacidad libre (capacidad: %d, ocupados: %d)",
                    oficinaId, fecha, hora, capacidad, ocupados));
        }
        this.ocupados++;
    }

    public void liberar() {
        if (this.ocupados > 0) {
            this.ocupados--;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getOficinaId() {
        return oficinaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getOcupados() {
        return ocupados;
    }

    public int getVersion() {
        return version;
    }
}

