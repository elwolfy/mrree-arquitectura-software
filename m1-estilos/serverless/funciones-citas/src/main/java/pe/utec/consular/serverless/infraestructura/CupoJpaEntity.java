package pe.utec.consular.serverless.infraestructura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "cupo", schema = "citas")
public class CupoJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "oficina_id", length = 5, nullable = false)
    private String oficinaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "capacidad", nullable = false)
    private int capacidad;

    @Column(name = "ocupados", nullable = false)
    private int ocupados;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    protected CupoJpaEntity() {
    }

    public CupoJpaEntity(
            UUID id,
            String oficinaId,
            LocalDate fecha,
            LocalTime hora,
            int capacidad,
            int ocupados,
            int version
    ) {
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

