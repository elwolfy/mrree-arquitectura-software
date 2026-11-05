package pe.utec.consular.monolito.compartido.infraestructura;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para el envío de notificaciones y simulación de fallas.
 * <p>Concepto: Patrón Adapter y variación de atributos de calidad (secciones 1.1 y 2.1 del plan).
 */
@ConfigurationProperties(prefix = "notificaciones")
public class NotificacionesProperties {

    /**
     * Canal activo: CONSOLA o CORREO.
     */
    private String canal = "CONSOLA";

    /**
     * Activa la simulación de fallas en el envío de notificaciones para comparar estilos.
     */
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
