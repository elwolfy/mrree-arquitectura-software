package pe.utec.consular.citas.domain;

import pe.utec.consular.citas.domain.exception.ReglaNegocioException;

/**
 * Objeto de valor inmutable que representa el Documento Nacional de Identidad (8 dígitos).
 */
public record Dni(String valor) {

    public Dni {
        if (valor == null || !valor.matches("^\\d{8}$")) {
            throw new ReglaNegocioException("El DNI debe contener exactamente 8 dígitos numéricos");
        }
    }

    public String enmascarado() {
        return valor.substring(0, 4) + "****";
    }

    @Override
    public String toString() {
        return valor;
    }
}

