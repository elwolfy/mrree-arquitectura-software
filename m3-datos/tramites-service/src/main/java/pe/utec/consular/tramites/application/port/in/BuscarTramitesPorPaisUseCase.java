package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.Tramite;

import java.util.List;

/**
 * Puerto de entrada para buscar trámites mediante consulta documental JSONB ({@code @>})
 * sobre {@code tramites.expediente.metadatos}.
 */
public interface BuscarTramitesPorPaisUseCase {

    List<Tramite> buscarPorPais(String pais);
}

