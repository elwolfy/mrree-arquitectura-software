package pe.utec.consular.clean.citas.frameworks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import pe.utec.consular.clean.citas.entities.ModoConsistencia;

/**
 * Propiedades de configuración para el modo de consistencia CAP (sección 4.2 y 4.3 del contrato global).
 */
@ConfigurationProperties(prefix = "consistencia")
public class ConsistenciaProperties {

    private ModoConsistencia modo = ModoConsistencia.CP;

    public ModoConsistencia getModo() {
        return modo;
    }

    public void setModo(ModoConsistencia modo) {
        this.modo = modo != null ? modo : ModoConsistencia.CP;
    }
}

