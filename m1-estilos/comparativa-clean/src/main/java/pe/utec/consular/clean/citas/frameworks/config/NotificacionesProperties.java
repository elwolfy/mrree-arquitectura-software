package pe.utec.consular.clean.citas.frameworks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para la simulación de fallas de notificación (sección 4.3 del contrato global).
 */
@ConfigurationProperties(prefix = "notificaciones")
public class NotificacionesProperties {

    private String canal = "CONSOLA";
    private boolean simularFalla = false;

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public boolean isSimularFalla() {
        return simularFalla;
    }

    public void setSimularFalla(boolean simularFalla) {
        this.simularFalla = simularFalla;
    }
}

