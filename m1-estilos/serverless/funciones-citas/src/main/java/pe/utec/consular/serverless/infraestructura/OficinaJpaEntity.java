package pe.utec.consular.serverless.infraestructura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "oficina_consular", schema = "citas")
public class OficinaJpaEntity {

    @Id
    @Column(name = "id", length = 5, nullable = false)
    private String id;

    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "pais", length = 2, nullable = false)
    private String pais;

    @Column(name = "ciudad", length = 60, nullable = false)
    private String ciudad;

    @Column(name = "zona_horaria", length = 40, nullable = false)
    private String zonaHoraria;

    protected OficinaJpaEntity() {
    }

    public OficinaJpaEntity(String id, String nombre, String pais, String ciudad, String zonaHoraria) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.ciudad = ciudad;
        this.zonaHoraria = zonaHoraria;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPais() {
        return pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }
}

