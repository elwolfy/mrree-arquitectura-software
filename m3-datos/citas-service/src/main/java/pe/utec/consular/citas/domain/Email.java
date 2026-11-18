package pe.utec.consular.citas.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Objeto de valor que representa una dirección de correo electrónico válida.
 * <p>Concepto: Value Object (DDD) autovalidado en el contexto de Citas (sección 2.4).
 * <p>Sesión: S3.
 */
public record Email(String valor) {

    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        Objects.requireNonNull(valor, "El correo electrónico no puede ser nulo");
        if (!PATRON_EMAIL.matcher(valor.trim()).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido: " + valor);
        }
        valor = valor.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return valor;
    }
}

