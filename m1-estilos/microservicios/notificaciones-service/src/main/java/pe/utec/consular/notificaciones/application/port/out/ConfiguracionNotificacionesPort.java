package pe.utec.consular.notificaciones.application.port.out;

/**
 * Puerto de salida para consultar la configuración de canal y simulación de falla.
 * <p>Concepto: Arquitectura hexagonal – aislamiento de propiedades de infraestructura tras un puerto.
 */
public interface ConfiguracionNotificacionesPort {

    String getCanal();

    boolean isSimularFalla();
}

