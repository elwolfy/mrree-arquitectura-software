package pe.utec.consular.clean.tarifas.usecase;

import pe.utec.consular.clean.tarifas.entities.CalculadoraDeTarifas;
import pe.utec.consular.clean.tarifas.entities.CalculadoraDeTarifas.ResultadoTarifa;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Interactor para calcular la tarifa consular delegando en {@link CalculadoraDeTarifas} y {@link TarifaBaseGateway}.
 * <p>Concepto: Clean Architecture – Interactor sin anotaciones de Spring (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code CalcularTarifaService} en {@code monolito-modular}.
 */
public class CalcularTarifaInteractor implements CalcularTarifaInputBoundary {

    private final CalculadoraDeTarifas calculadoraDeTarifas;
    private final TarifaBaseGateway tarifaBaseGateway;
    private final CalcularTarifaOutputBoundary outputBoundary;

    public CalcularTarifaInteractor(CalculadoraDeTarifas calculadoraDeTarifas,
                                    TarifaBaseGateway tarifaBaseGateway,
                                    CalcularTarifaOutputBoundary outputBoundary) {
        this.calculadoraDeTarifas = calculadoraDeTarifas;
        this.tarifaBaseGateway = tarifaBaseGateway;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void calcular(CalcularTarifaRequestModel requestModel) {
        Optional<BigDecimal> montoBase = tarifaBaseGateway.obtenerMontoBase(requestModel.tipoTramite());
        ResultadoTarifa resultado = calculadoraDeTarifas.calcular(
                requestModel.tipoTramite(),
                requestModel.pais(),
                montoBase
        );

        outputBoundary.presentar(new CalcularTarifaResponseModel(
                resultado.tipoTramite(),
                resultado.pais(),
                resultado.moneda(),
                resultado.monto(),
                resultado.estrategia()
        ));
    }
}

