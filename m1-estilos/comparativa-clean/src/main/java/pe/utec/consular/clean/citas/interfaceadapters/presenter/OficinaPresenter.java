package pe.utec.consular.clean.citas.interfaceadapters.presenter;

import pe.utec.consular.clean.citas.usecase.oficinas.ListarOficinasOutputBoundary;
import pe.utec.consular.clean.citas.usecase.oficinas.ListarOficinasResponseModel;

import java.util.List;

/**
 * Presentador que transforma la lista de {@link ListarOficinasResponseModel} en {@link OficinaViewModel}.
 * <p>Concepto: Clean Architecture – Presenter (sección 1.3 del plan).
 * <p>Sesión: S2.
 */
public class OficinaPresenter implements ListarOficinasOutputBoundary {

    private final ThreadLocal<List<OficinaViewModel>> ultimaLista = new ThreadLocal<>();

    @Override
    public void presentar(List<ListarOficinasResponseModel> oficinas) {
        List<OficinaViewModel> viewModels = oficinas.stream()
                .map(o -> new OficinaViewModel(o.id(), o.nombre(), o.pais(), o.ciudad(), o.zonaHoraria()))
                .toList();
        ultimaLista.set(viewModels);
    }

    public List<OficinaViewModel> obtenerViewModels() {
        List<OficinaViewModel> lista = ultimaLista.get();
        ultimaLista.remove();
        return lista;
    }
}

