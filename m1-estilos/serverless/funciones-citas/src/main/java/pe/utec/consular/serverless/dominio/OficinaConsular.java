package pe.utec.consular.serverless.dominio;

import java.util.Objects;

public record OficinaConsular(
        String id,
        String nombre,
        String pais,
        String ciudad,
        String zonaHoraria
) {
    public OficinaConsular {
        Objects.requireNonNull(id, "El ID de la oficina consular no puede ser nulo");
        Objects.requireNonNull(nombre, "El nombre de la oficina consular no puede ser nulo");
        Objects.requireNonNull(pais, "El país no puede ser nulo");
        Objects.requireNonNull(ciudad, "La ciudad no puede ser nula");
        Objects.requireNonNull(zonaHoraria, "La zona horaria no puede ser nula");
    }
}

