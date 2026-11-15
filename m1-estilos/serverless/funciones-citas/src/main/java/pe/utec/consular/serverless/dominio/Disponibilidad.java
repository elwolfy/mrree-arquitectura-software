package pe.utec.consular.serverless.dominio;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record Disponibilidad(
        String oficinaId,
        LocalDate fecha,
        String modoConsistencia,
        List<Cupo> cupos
) {
    public Disponibilidad {
        Objects.requireNonNull(oficinaId, "El ID de la oficina no puede ser nulo");
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        Objects.requireNonNull(modoConsistencia, "El modo de consistencia no puede ser nulo");
        cupos = List.copyOf(Objects.requireNonNull(cupos, "La lista de cupos no puede ser nula"));
    }
}

