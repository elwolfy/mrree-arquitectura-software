package pe.utec.consular.clean.citas.interfaceadapters.presenter;

import pe.utec.consular.clean.citas.usecase.cancelar.CancelarCitaOutputBoundary;
import pe.utec.consular.clean.citas.usecase.cancelar.CancelarCitaResponseModel;
import pe.utec.consular.clean.citas.usecase.consultar.ConsultarCitaOutputBoundary;
import pe.utec.consular.clean.citas.usecase.consultar.ConsultarCitaResponseModel;
import pe.utec.consular.clean.citas.usecase.registrar.RegistrarCitaOutputBoundary;
import pe.utec.consular.clean.citas.usecase.registrar.RegistrarCitaResponseModel;

import java.util.List;

/**
 * Presentador de citas consulares que implementa las fronteras de salida (Output Boundaries).
 * <p>Concepto: Clean Architecture – Patrón Output Boundary -> Presenter -> ViewModel (sección 1.3 del plan).
 * <p><b>Diferencia clave frente a la Arquitectura Hexagonal ({@code monolito-modular}):</b>
 * En la versión hexagonal, {@code RegistrarCitaService.registrar(comando)} devuelve directamente
 * la entidad {@code Cita} al {@code CitaController}, el cual usa {@code CitaWebMapper} para convertirla a {@code CitaDto}.
 * En Clean Architecture, el {@code RegistrarCitaInteractor} no devuelve nada al controlador: invoca a
 * {@link RegistrarCitaOutputBoundary#presentar(RegistrarCitaResponseModel)}, y este {@code CitaPresenter}
 * transforma el {@code ResponseModel} en un {@link CitaViewModel} listo para serializarse como JSON.
 * <p>Se emplea {@link ThreadLocal} para aislar el {@code ViewModel} de cada petición HTTP concurrente
 * permitiendo a la vez que el presentador sea inyectado como un bean sencillo sin anotaciones de Spring en {@code usecase}.
 * <p>Sesión: S2. Compárese con {@code pe.utec.consular.monolito.citas.adapter.in.web.CitaWebMapper}.
 */
public class CitaPresenter implements
        RegistrarCitaOutputBoundary,
        ConsultarCitaOutputBoundary,
        CancelarCitaOutputBoundary {

    private final ThreadLocal<CitaViewModel> ultimoViewModel = new ThreadLocal<>();
    private final ThreadLocal<List<CitaViewModel>> ultimaListaViewModel = new ThreadLocal<>();
    private final ThreadLocal<CancelarCitaResponseModel> ultimaCancelacion = new ThreadLocal<>();

    @Override
    public void presentar(RegistrarCitaResponseModel responseModel) {
        ultimoViewModel.set(new CitaViewModel(
                responseModel.id(),
                responseModel.codigo(),
                responseModel.dni(),
                responseModel.nombres(),
                responseModel.oficinaId(),
                responseModel.fecha(),
                responseModel.hora(),
                responseModel.tipoTramite(),
                responseModel.estado(),
                responseModel.notificacion(),
                responseModel.creadaEn()
        ));
    }

    @Override
    public void presentarCita(ConsultarCitaResponseModel responseModel) {
        ultimoViewModel.set(aViewModel(responseModel));
    }

    @Override
    public void presentarListaCitas(List<ConsultarCitaResponseModel> responseModels) {
        ultimaListaViewModel.set(responseModels.stream()
                .map(this::aViewModel)
                .toList());
    }

    @Override
    public void presentarCancelacion(CancelarCitaResponseModel responseModel) {
        ultimaCancelacion.set(responseModel);
    }

    public CitaViewModel obtenerViewModel() {
        CitaViewModel vm = ultimoViewModel.get();
        ultimoViewModel.remove();
        return vm;
    }

    public List<CitaViewModel> obtenerListaViewModel() {
        List<CitaViewModel> lista = ultimaListaViewModel.get();
        ultimaListaViewModel.remove();
        return lista;
    }

    public CancelarCitaResponseModel obtenerCancelacion() {
        CancelarCitaResponseModel cancelacion = ultimaCancelacion.get();
        ultimaCancelacion.remove();
        return cancelacion;
    }

    private CitaViewModel aViewModel(ConsultarCitaResponseModel r) {
        return new CitaViewModel(
                r.id(),
                r.codigo(),
                r.dni(),
                r.nombres(),
                r.oficinaId(),
                r.fecha(),
                r.hora(),
                r.tipoTramite(),
                r.estado(),
                r.notificacion(),
                r.creadaEn()
        );
    }
}

