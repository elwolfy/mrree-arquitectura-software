package pe.utec.consular.monolito.citas.application.port.in;

import pe.utec.consular.monolito.citas.domain.OficinaConsular;

import java.util.List;

/**
 * Puerto de entrada primario para listar las oficinas consulares habilitadas.
 * <p>Concepto: Arquitectura hexagonal – puerto de entrada (sección 1.3).
 */
public interface ListarOficinasUseCase {

    List<OficinaConsular> listar();
}
