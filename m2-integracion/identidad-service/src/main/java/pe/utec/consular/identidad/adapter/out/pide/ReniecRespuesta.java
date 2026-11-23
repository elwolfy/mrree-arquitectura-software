package pe.utec.consular.identidad.adapter.out.pide;

/**
 * DTO externo que modela la estructura y nomenclatura deliberadamente ajena
 * devuelta por el servicio legado de consulta de RENIEC en la PIDE simulada.
 * <p>Ninguna clase fuera del paquete {@code adapter.out.pide} conoce campos como
 * {@code coResultado}, {@code prenombres}, {@code apPrimer}, {@code apSegundo} o {@code inFallecido}.
 */
public record ReniecRespuesta(
        String coResultado,
        String deResultado,
        DatosPersonaPide datosPersona
) {
    public record DatosPersonaPide(
            String nuDni,
            String prenombres,
            String apPrimer,
            String apSegundo,
            String estadoCivil,
            String inFallecido
    ) {
    }
}

