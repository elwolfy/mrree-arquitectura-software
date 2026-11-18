package pe.utec.consular.citas.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Objeto de valor que representa el código identificador de una cita consular.
 * Formato del contrato global (sección 4.2): CIT-YYYY-NNNNNN (ej. CIT-2026-000123).
 * <p>Concepto: Value Object con invariantes de formato (sección 1.3).
 * <p>Sesión: S3.
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

