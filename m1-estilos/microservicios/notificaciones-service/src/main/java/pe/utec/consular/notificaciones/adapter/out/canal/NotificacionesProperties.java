package pe.utec.consular.notificaciones.adapter.out.canal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import pe.utec.consular.notificaciones.application.port.out.ConfiguracionNotificacionesPort;

/**
 * Propiedades de configuración para el envío de notificaciones y simulación de fallas.
 * <p>Concepto: Patrón Adapter y variación de atributos de calidad (secciones 1.1 y 2.1 del plan).
 */
@ConfigurationProperties(prefix = "notificaciones")
public class NotificacionesProperties implements ConfiguracionNotificacionesPort {

    private String canal = "CONSOLA";
    private boolean simularFalla = false;

    @Override
    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    @Override
    public boolean isSimularFalla() {
        return simularFalla;
    }

    public void setSimularFalla(boolean simularFalla) {
        this.simularFalla = simularFalla;
    }
}

