package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

/**
 * Objeto de valor que representa el código correlativo de un trámite consular (TRM-YYYY-NNNNNN).
 */
public record CodigoTramite(String valor) {

    public CodigoTramite {
        if (valor == null || !valor.matches("^TRM-\\d{4}-\\d{6}$")) {
            throw new SolicitudInvalidaException("Formato de código de trámite inválido (esperado TRM-YYYY-NNNNNN): " + valor);
        }
    }

    public static CodigoTramite generar(int anio, long secuencia) {
        return new CodigoTramite(String.format("TRM-%04d-%06d", anio, secuencia));
    }

    @Override
    public String toString() {
        return valor;
    }
}

