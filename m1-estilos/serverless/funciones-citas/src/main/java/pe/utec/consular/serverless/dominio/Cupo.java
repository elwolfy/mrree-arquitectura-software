package pe.utec.consular.serverless.dominio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public class Cupo {

    private final UUID id;
    private final String oficinaId;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final int capacidad;
    private int ocupados;
    private final int version;

    public Cupo(
            UUID id,
            String oficinaId,
            LocalDate fecha,
            LocalTime hora,
            int capacidad,
            int ocupados,
            int version
    ) {
        this.id = Objects.requireNonNull(id, "El ID del cupo no puede ser nulo");
        this.oficinaId = Objects.requireNonNull(oficinaId, "El ID de la oficina no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha del cupo no puede ser nula");
        this.hora = Objects.requireNonNull(hora, "La hora del cupo no puede ser nula");
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad del cupo debe ser positiva");
        }
        if (ocupados < 0 || ocupados > capacidad) {
            throw new IllegalArgumentException("El número de ocupados está fuera de rango");
        }
        this.capacidad = capacidad;
        this.ocupados = ocupados;
        this.version = version;
    }

    public int disponibles() {
        return capacidad - ocupados;
    }

    public void ocuparPlaza() {
        if (ocupados >= capacidad) {
            throw new CupoAgotadoException(oficinaId, fecha, hora);
        }
        this.ocupados++;
    }

    public void liberarPlaza() {
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

