package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;

/**
 * Puerto de entrada para listar el catálogo de oficinas consulares.
 */
public interface ListarOficinasUseCase {

    List<OficinaConsular> listarTodas();
}

