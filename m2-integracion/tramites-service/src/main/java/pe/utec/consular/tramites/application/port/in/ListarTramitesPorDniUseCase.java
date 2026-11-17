package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.Tramite;

import java.util.List;

/**
 * Puerto de entrada para listar los trámites consulares de un ciudadano por DNI.
 */
public interface ListarTramitesPorDniUseCase {

    List<Tramite> listarPorDni(String dni);
}

