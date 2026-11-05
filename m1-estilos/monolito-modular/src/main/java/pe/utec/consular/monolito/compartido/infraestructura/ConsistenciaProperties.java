package pe.utec.consular.monolito.compartido.infraestructura;

import org.springframework.boot.context.properties.ConfigurationProperties;
import pe.utec.consular.monolito.compartido.domain.ModoConsistencia;

/**
 * Propiedades de configuración para el teorema CAP y consistencia del sistema.
 * <p>Concepto: Teorema CAP (sección 1.4 del plan).
 */
@ConfigurationProperties(prefix = "consistencia")
public class ConsistenciaProperties {

    /**
     * Modo de consistencia actual: CP (consistencia) o AP (alta disponibilidad con caché).
     */
    private ModoConsistencia modo = ModoConsistencia.CP;

    public ModoConsistencia getModo() {
        return modo;
    }

    public void setModo(ModoConsistencia modo) {
        this.modo = modo;
    }
}
