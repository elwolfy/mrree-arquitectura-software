package pe.utec.consular.serverless.dominio;

import java.util.Objects;
import java.util.regex.Pattern;

public record Dni(String valor) {

    private static final Pattern PATRON_DNI = Pattern.compile("^\\d{8}$");

    public Dni {
        Objects.requireNonNull(valor, "El DNI no puede ser nulo");
        if (!PATRON_DNI.matcher(valor).matches()) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos numéricos: " + valor);
        }
    }
}

