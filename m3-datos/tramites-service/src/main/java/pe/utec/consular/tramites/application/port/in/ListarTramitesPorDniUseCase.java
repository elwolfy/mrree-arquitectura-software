package pe.utec.consular.tramites.application.port.in;

import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.List;

/**
 * Puerto de entrada para listar los trámites de un ciudadano por DNI,
 * leyendo exclusivamente de la proyección CQRS ({@code tramites.vista_tramite}).
 */
public interface ListarTramitesPorDniUseCase {

    List<VistaTramite> listarPorDni(String dni);
}
