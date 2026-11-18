package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

/**
 * Objeto de valor para el Documento Nacional de Identidad (8 dígitos numéricos).
 */
public record Dni(String valor) {

    public Dni {
        if (valor == null || !valor.matches("^\\d{8}$")) {
            throw new SolicitudInvalidaException("El DNI debe contener exactamente 8 dígitos numéricos");
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

