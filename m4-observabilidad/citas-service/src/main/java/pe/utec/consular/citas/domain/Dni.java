package pe.utec.consular.citas.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Objeto de valor que representa el Documento Nacional de Identidad peruano en el contexto de Citas.
 * <p>Concepto: Value Object (DDD) propio del Bounded Context, evitando un Kernel Compartido (sección 2.4).
 * <p>Sesión: S3.
 */
public record Dni(String valor) {

    private static final Pattern PATRON_DNI = Pattern.compile("^\\d{8}$");

    public Dni {
        Objects.requireNonNull(valor, "El DNI no puede ser nulo");
        if (!PATRON_DNI.matcher(valor.trim()).matches()) {
            throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos numéricos: " + valor);
        }
        valor = valor.trim();
    }

    /**
     * Enmascara el DNI para uso seguro en logs de auditoría según regla 02.
     * Ejemplo: "40000001" -> "4000****".
     */
    public String enmascarado() {
        return valor.substring(0, 4) + "****";
    }

    @Override
    public String toString() {
        return valor;
    }
}

