package pe.utec.consular.citas.domain;

import java.util.Objects;

/**
 * Entidad de dominio que representa una oficina consular en el exterior.
 */
public class OficinaConsular {

    private final String id;
    private final String nombre;
    private final String pais;
    private final String ciudad;
    private final String zonaHoraria;

    public OficinaConsular(String id, String nombre, String pais, String ciudad, String zonaHoraria) {
        this.id = Objects.requireNonNull(id, "El id de oficina no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.pais = Objects.requireNonNull(pais, "El país no puede ser nulo");
        this.ciudad = Objects.requireNonNull(ciudad, "La ciudad no puede ser nula");
        this.zonaHoraria = Objects.requireNonNull(zonaHoraria, "La zona horaria no puede ser nula");
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

