package pe.utec.consular.citas.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code citas.cupo}.
 */
@Entity
@Table(name = "cupo", schema = "citas")
public class CupoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "oficina_id", nullable = false, length = 5)
    private String oficinaId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false)
    private int capacidad;

    @Column(nullable = false)
    private int ocupados;

    @Version
    @Column(nullable = false)
    private int version;

    public CupoJpaEntity() {
    }

    public CupoJpaEntity(UUID id, String oficinaId, LocalDate fecha, LocalTime hora,
                         int capacidad, int ocupados, int version) {
        this.id = id;
        this.oficinaId = oficinaId;
        this.fecha = fecha;
        this.hora = hora;
        this.capacidad = capacidad;
        this.ocupados = ocupados;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOficinaId() {
        return oficinaId;
    }

    public void setOficinaId(String oficinaId) {
        this.oficinaId = oficinaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getOcupados() {
        return ocupados;
    }

    public void setOcupados(int ocupados) {
        this.ocupados = ocupados;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}

