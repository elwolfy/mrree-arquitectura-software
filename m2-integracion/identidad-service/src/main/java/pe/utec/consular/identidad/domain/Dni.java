package pe.utec.consular.identidad.domain;

/**
 * Objeto de valor para el Documento Nacional de Identidad (8 dígitos numéricos).
 */
public record Dni(String valor) {

    public Dni {
        if (valor == null || !valor.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos numéricos");
        }
    }

    public String enmascarado() {
        return "****" + valor.substring(4);
    }

    @Override
    public String toString() {
        return valor;
    }
}

