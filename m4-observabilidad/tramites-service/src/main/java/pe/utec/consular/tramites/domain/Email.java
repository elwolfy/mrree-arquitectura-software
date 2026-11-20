package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

/**
 * Objeto de valor para la dirección de correo electrónico del ciudadano.
 */
public record Email(String valor) {

    public Email {
        if (valor == null || !valor.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new SolicitudInvalidaException("El correo electrónico proporcionado no tiene un formato válido");
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}

