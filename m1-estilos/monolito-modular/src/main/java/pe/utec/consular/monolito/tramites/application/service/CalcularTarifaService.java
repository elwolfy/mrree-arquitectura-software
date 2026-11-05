package pe.utec.consular.monolito.tramites.application.service;

import org.springframework.stereotype.Service;
import pe.utec.consular.monolito.compartido.domain.TipoTramite;
import pe.utec.consular.monolito.tramites.application.port.in.CalcularTarifaUseCase;
import pe.utec.consular.monolito.tramites.domain.tarifa.CalculadoraDeTarifas;
import pe.utec.consular.monolito.tramites.domain.tarifa.CalculadoraDeTarifas.ResultadoTarifa;

/**
 * Servicio de aplicación para calcular tarifas consulares delegando en la calculadora de estrategias.
 * <p>Concepto: Arquitectura hexagonal – caso de uso (sección 1.3).
 */
@Service
public class CalcularTarifaService implements CalcularTarifaUseCase {

    private final CalculadoraDeTarifas calculadoraDeTarifas;

    public CalcularTarifaService(CalculadoraDeTarifas calculadoraDeTarifas) {
        this.calculadoraDeTarifas = calculadoraDeTarifas;
    }

    @Override
    public ResultadoTarifa calcular(TipoTramite tipoTramite, String pais) {
        return calculadoraDeTarifas.calcular(tipoTramite, pais);
    }
}
