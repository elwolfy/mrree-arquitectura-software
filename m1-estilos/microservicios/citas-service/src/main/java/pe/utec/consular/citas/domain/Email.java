package pe.utec.consular.citas.domain;

import pe.utec.consular.citas.domain.exception.ReglaNegocioException;

import java.util.regex.Pattern;

/**
 * Objeto de valor inmutable para el correo electrónico del ciudadano.
 */
public record Email(String valor) {

    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (valor == null || !PATRON_EMAIL.matcher(valor).matches()) {
            throw new ReglaNegocioException("El formato del correo electrónico no es válido: " + valor);
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}

