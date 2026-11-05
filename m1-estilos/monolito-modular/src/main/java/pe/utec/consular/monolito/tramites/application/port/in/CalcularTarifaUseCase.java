package pe.utec.consular.monolito.tramites.application.port.in;

import pe.utec.consular.monolito.compartido.domain.TipoTramite;
import pe.utec.consular.monolito.tramites.domain.tarifa.CalculadoraDeTarifas.ResultadoTarifa;

/**
 * Puerto de entrada primario para el cálculo de tarifas consulares.
 * <p>Concepto: Arquitectura hexagonal – puerto de entrada (sección 1.3 del plan).
 * <p>Sesión: S2.
 */
public interface CalcularTarifaUseCase {

    ResultadoTarifa calcular(TipoTramite tipoTramite, String pais);
}
