package pe.utec.consular.notificaciones.application.port.out;

import pe.utec.consular.notificaciones.domain.Aviso;

/**
 * Puerto de salida para el canal de despacho de notificaciones (Consola o Correo).
 * <p>Concepto: Patrón Adapter y puerto secundario en arquitectura hexagonal (secciones 1.3 y 2.1).
 */
public interface CanalDeEnvioPort {

    void enviar(Aviso aviso);
}

