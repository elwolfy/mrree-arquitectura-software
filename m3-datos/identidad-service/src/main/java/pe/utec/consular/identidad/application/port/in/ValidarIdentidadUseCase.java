package pe.utec.consular.identidad.application.port.in;

import pe.utec.consular.identidad.domain.Ciudadano;

/**
 * Puerto de entrada para validar la identidad de un ciudadano por su número de DNI.
 */
public interface ValidarIdentidadUseCase {

    Ciudadano validar(String dni);
}

