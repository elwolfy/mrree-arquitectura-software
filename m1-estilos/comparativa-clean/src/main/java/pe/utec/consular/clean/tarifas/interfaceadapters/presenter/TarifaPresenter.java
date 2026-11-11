package pe.utec.consular.clean.tarifas.interfaceadapters.presenter;

import pe.utec.consular.clean.tarifas.usecase.CalcularTarifaOutputBoundary;
import pe.utec.consular.clean.tarifas.usecase.CalcularTarifaResponseModel;

/**
 * Presentador de tarifas que transforma {@link CalcularTarifaResponseModel} en {@link TarifaViewModel}.
 * <p>Concepto: Clean Architecture – Presenter (sección 1.3 del plan).
 */
public class TarifaPresenter implements CalcularTarifaOutputBoundary {

    private final ThreadLocal<TarifaViewModel> ultimoViewModel = new ThreadLocal<>();

    @Override
    public void presentar(CalcularTarifaResponseModel responseModel) {
        ultimoViewModel.set(new TarifaViewModel(
                responseModel.tipoTramite(),
                responseModel.pais(),
                responseModel.moneda(),
                responseModel.monto(),
                responseModel.estrategia()
        ));
    }

    public TarifaViewModel obtenerViewModel() {
        TarifaViewModel vm = ultimoViewModel.get();
        ultimoViewModel.remove();
        return vm;
    }
}

