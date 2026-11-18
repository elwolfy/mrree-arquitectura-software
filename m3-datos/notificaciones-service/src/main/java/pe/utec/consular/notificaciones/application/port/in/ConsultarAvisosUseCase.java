package pe.utec.consular.notificaciones.application.port.in;

import pe.utec.consular.notificaciones.domain.Aviso;

import java.util.List;

/**
 * Puerto de entrada para consultar los avisos enviados a un ciudadano por su DNI.
 */
public interface ConsultarAvisosUseCase {

    List<Aviso> listarPorDni(String dni);
}

