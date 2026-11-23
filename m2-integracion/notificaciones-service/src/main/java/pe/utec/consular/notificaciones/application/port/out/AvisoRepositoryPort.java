package pe.utec.consular.notificaciones.application.port.out;

import pe.utec.consular.notificaciones.domain.Aviso;

import java.util.List;

/**
 * Puerto de salida para persistir y consultar avisos.
 */
public interface AvisoRepositoryPort {

    Aviso guardar(Aviso aviso);

    List<Aviso> buscarPorDni(String dni);
}

