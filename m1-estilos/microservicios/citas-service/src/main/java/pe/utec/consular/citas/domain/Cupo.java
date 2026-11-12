package pe.utec.consular.citas.domain;

import pe.utec.consular.citas.domain.exception.CupoSinCapacidadException;
import pe.utec.consular.citas.domain.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un cupo horario con capacidad limitada en una oficina consular.
 * <p>Concepto: Invariante de dominio para control de capacidad de cupos (regla 1 de la sección 4.2 del contrato global).
 * <p>Sesión: S1.
 */
public class Cupo {

    private final UUID id;
    private final String oficinaId;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final int capacidad;
    private int ocupados;
    private final int version;

    public Cupo(UUID id, String oficinaId, LocalDate fecha, LocalTime hora,
                int capacidad, int ocupados, int version) {
        this.id = Objects.requireNonNull(id, "El id del cupo no puede ser nulo");
        this.oficinaId = Objects.requireNonNull(oficinaId, "El id de oficina no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.hora = Objects.requireNonNull(hora, "La hora no puede ser nula");
        if (capacidad <= 0) {
            throw new ReglaNegocioException("La capacidad del cupo debe ser mayor a cero");
        }
        if (ocupados < 0 || ocupados > capacidad) {
            throw new ReglaNegocioException("La cantidad de ocupados es inválida para la capacidad del cupo");
        }
        this.capacidad = capacidad;
        this.ocupados = ocupados;
        this.version = version;
    }

    public static Cupo nuevo(String oficinaId, LocalDate fecha, LocalTime hora, int capacidad) {
        return new Cupo(UUID.randomUUID(), oficinaId, fecha, hora, capacidad, 0, 0);
    }

    public boolean cupoDisponible() {
        return ocupados < capacidad;
    }

    public int cuposLibres() {
        return capacidad - ocupados;
    }

    public void reservar() {
        if (!cupoDisponible()) {
            throw new CupoSinCapacidadException(String.format(
                    "El cupo de las %s del %s en la oficina %s no cuenta con capacidad disponible (%d/%d)",
                    hora, fecha, oficinaId, ocupados, capacidad));
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

