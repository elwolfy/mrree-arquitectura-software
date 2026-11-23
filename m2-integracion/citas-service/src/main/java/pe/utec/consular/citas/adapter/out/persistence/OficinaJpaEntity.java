package pe.utec.consular.citas.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entidad JPA para la tabla citas.oficina_consular.
 */
@Entity
@Table(name = "oficina_consular", schema = "citas")
public class OficinaJpaEntity {

    @Id
    @Column(length = 5)
    private String id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 2)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String pais;

    @Column(nullable = false, length = 60)
    private String ciudad;

    @Column(name = "zona_horaria", nullable = false, length = 40)
    private String zonaHoraria;

    public OficinaJpaEntity() {
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

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }
}

