package pe.utec.consular.clean.citas.entities;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Objeto de valor que representa el Documento Nacional de Identidad (8 dígitos).
 * <p>Concepto: Clean Architecture – Círculo 1 ({@code entities}, sección 1.3 del plan).
 * <p>Sesión: S2.
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

    public String enmascarado() {
        return valor.substring(0, 4) + "****";
    }

    @Override
    public String toString() {
        return valor;
    }
}

