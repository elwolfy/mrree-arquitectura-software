package pe.utec.consular.clean.citas.entities;

import java.util.Objects;

/**
 * Entidad empresarial que representa una Oficina Consular del Perú en el exterior.
 * <p>Concepto: Clean Architecture – Círculo 1 ({@code entities}, sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.domain.OficinaConsular}.
 */
public class OficinaConsular {

    private final String id;
    private final String nombre;
    private final String pais;
    private final String ciudad;
    private final String zonaHoraria;

    public OficinaConsular(String id, String nombre, String pais, String ciudad, String zonaHoraria) {
        this.id = Objects.requireNonNull(id, "El identificador de oficina no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la oficina no puede ser nulo");
        this.pais = Objects.requireNonNull(pais, "El código de país no puede ser nulo");
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

