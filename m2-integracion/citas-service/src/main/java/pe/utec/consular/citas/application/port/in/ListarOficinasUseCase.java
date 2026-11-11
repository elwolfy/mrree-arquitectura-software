package pe.utec.consular.citas.application.port.in;

import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;

/**
 * Puerto de entrada para listar el catálogo de oficinas consulares.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3).
 * <p>Sesión: S3.
 */
public interface ListarOficinasUseCase {

    List<OficinaConsular> listar();
}

