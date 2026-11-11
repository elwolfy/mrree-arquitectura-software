package pe.utec.consular.clean.citas.entities;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Objeto de valor que representa el código identificador de una cita consular (CIT-YYYY-NNNNNN).
 * <p>Concepto: Clean Architecture – Círculo 1 ({@code entities}, sección 1.3 del plan).
 * <p>Sesión: S2.
 */
public record CodigoCita(String valor) {

    private static final Pattern PATRON_CODIGO = Pattern.compile("^CIT-\\d{4}-\\d{6}$");

    public CodigoCita {
        Objects.requireNonNull(valor, "El código de cita no puede ser nulo");
        if (!PATRON_CODIGO.matcher(valor.trim()).matches()) {
            throw new IllegalArgumentException("Formato de código de cita inválido (esperado CIT-YYYY-NNNNNN): " + valor);
        }
        valor = valor.trim();
    }

    public static CodigoCita generar(int anio, long secuencia) {
        return new CodigoCita(String.format("CIT-%04d-%06d", anio, secuencia));
    }

    @Override
    public String toString() {
        return valor;
    }
}

