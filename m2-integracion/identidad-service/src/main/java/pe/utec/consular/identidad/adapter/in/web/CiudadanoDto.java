package pe.utec.consular.identidad.adapter.in.web;

import pe.utec.consular.identidad.domain.Ciudadano;

/**
 * DTO de respuesta de validación de identidad expresado en el lenguaje ubicuo del dominio.
 */
public record CiudadanoDto(
        String dni,
        String nombres,
        String apellidos,
        boolean vigente
) {
    public static CiudadanoDto desdeDominio(Ciudadano ciudadano) {
        return new CiudadanoDto(
                ciudadano.dni(),
                ciudadano.nombres(),
                ciudadano.apellidos(),
                ciudadano.vigente()
        );
    }
}

