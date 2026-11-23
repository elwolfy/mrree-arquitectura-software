package pe.utec.consular.monolito.notificaciones.application.port.out;

import pe.utec.consular.monolito.notificaciones.domain.Aviso;

/**
 * Puerto secundario para el canal de transporte físico de notificaciones.
 * <p>Concepto: Patrón Adapter (sección 2.1 del plan) y puerto secundario hexagonal (1.3).
 * <p>Sesión: S2. Permite intercambiar consola o correo sin modificar la lógica de negocio.
 */
public interface CanalDeEnvioPort {

    /**
     * Remite el aviso por el canal correspondiente.
     *
     * @param aviso entidad del aviso a transmitir
     */
    void enviar(Aviso aviso);
}
