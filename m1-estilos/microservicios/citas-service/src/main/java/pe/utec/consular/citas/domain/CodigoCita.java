package pe.utec.consular.citas.domain;

import pe.utec.consular.citas.domain.exception.ReglaNegocioException;

/**
 * Objeto de valor que representa el código correlativo de una cita ({@code CIT-<año>-<6 dígitos>}).
 * <p>Concepto: Value Object inmutable en el dominio.
 */
public record CodigoCita(String valor) {

    public CodigoCita {
        if (valor == null || !valor.matches("^CIT-\\d{4}-\\d{6}$")) {
            throw new ReglaNegocioException("Formato de código de cita inválido: " + valor);
        }
    }

    public static CodigoCita generar(int anio, long secuencia) {
        return new CodigoCita(String.format("CIT-%04d-%06d", anio, secuencia));
    }

    @Override
    public String toString() {
        return valor;
    }
}

