package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.List;

/**
 * Puerto de entrada para buscar trámites mediante consulta documental JSONB ({@code @>})
 * sobre {@code tramites.expediente.metadatos} cruzada con {@code tramites.vista_tramite}.
 */
public interface BuscarTramitesPorPaisUseCase {

    List<VistaTramite> buscarPorPais(String pais);
}
