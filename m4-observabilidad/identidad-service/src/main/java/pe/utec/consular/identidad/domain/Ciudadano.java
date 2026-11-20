package pe.utec.consular.identidad.domain;

import java.util.Objects;

/**
 * Modelo propio del dominio consular que representa la identidad validada de un ciudadano.
 * <p>Concepto: Lenguaje ubicuo del dominio (sección 1.1), libre de cualquier nomenclatura
 * del padrón externo consultado a través de la PIDE.
 * <p>Sesión: S3.
 */
public record Ciudadano(
        String dni,
        String nombres,
        String apellidos,
        boolean vigente
) {
    public Ciudadano {
        Objects.requireNonNull(dni, "El DNI no puede ser nulo");
        Objects.requireNonNull(nombres, "Los nombres no pueden ser nulos");
        Objects.requireNonNull(apellidos, "Los apellidos no pueden ser nulos");
    }
}

