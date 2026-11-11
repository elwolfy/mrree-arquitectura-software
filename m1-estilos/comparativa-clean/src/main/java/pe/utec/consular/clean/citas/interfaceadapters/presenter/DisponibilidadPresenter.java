package pe.utec.consular.clean.citas.interfaceadapters.presenter;

import pe.utec.consular.clean.citas.interfaceadapters.presenter.DisponibilidadViewModel.CupoDisponibilidadViewModel;
import pe.utec.consular.clean.citas.usecase.disponibilidad.ConsultarDisponibilidadOutputBoundary;
import pe.utec.consular.clean.citas.usecase.disponibilidad.ConsultarDisponibilidadResponseModel;

import java.util.List;

/**
 * Presentador que transforma el {@link ConsultarDisponibilidadResponseModel} en {@link DisponibilidadViewModel}.
 * <p>Concepto: Clean Architecture – Presenter (sección 1.3 del plan).
 * <p>Sesión: S2.
 */
public class DisponibilidadPresenter implements ConsultarDisponibilidadOutputBoundary {

    private final ThreadLocal<DisponibilidadViewModel> ultimoViewModel = new ThreadLocal<>();

    @Override
    public void presentar(ConsultarDisponibilidadResponseModel responseModel) {
        List<CupoDisponibilidadViewModel> cuposVm = responseModel.cupos().stream()
                .map(c -> new CupoDisponibilidadViewModel(c.hora(), c.capacidad(), c.ocupados(), c.libres()))
                .toList();

        ultimoViewModel.set(new DisponibilidadViewModel(
                responseModel.oficinaId(),
                responseModel.fecha(),
                cuposVm,
                responseModel.origen(),
                responseModel.actualizadoHaceSegundos()
        ));
    }

    public DisponibilidadViewModel obtenerViewModel() {
        DisponibilidadViewModel vm = ultimoViewModel.get();
        ultimoViewModel.remove();
        return vm;
    }
}

