package pe.utec.consular.serverless.dominio;

import java.util.Objects;
import java.util.regex.Pattern;

public record CodigoCita(String valor) {

    private static final Pattern PATRON_CODIGO = Pattern.compile("^CITA-[A-Z]{3}-\\d{4}-\\d{6}$");

    public CodigoCita {
        Objects.requireNonNull(valor, "El código de cita no puede ser nulo");
        if (!PATRON_CODIGO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Formato de código de cita inválido: " + valor);
        }
    }

    public static CodigoCita generar(String oficinaId, int anio, long correlativo) {
        return new CodigoCita("CITA-%s-%04d-%06d".formatted(oficinaId, anio, correlativo));
    }
}

