package pe.utec.consular.serverless.dominio;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String valor) {

    private static final Pattern PATRON_EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        Objects.requireNonNull(valor, "El correo electrónico no puede ser nulo");
        if (valor.isBlank() || valor.length() > 120 || !PATRON_EMAIL.matcher(valor).matches()) {
            throw new IllegalArgumentException("Formato de correo electrónico inválido: " + valor);
        }
    }
}

