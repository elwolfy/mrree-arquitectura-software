package pe.utec.consular.notificaciones.application.port.in;

import pe.utec.consular.notificaciones.domain.Aviso;

import java.util.List;

/**
 * Puerto de entrada para consultar avisos registrados por referencia.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3 del plan).
 * <p>Sesión: S1.
 */
public interface ConsultarAvisosUseCase {

    List<Aviso> consultarPorReferencia(String referencia);
}

