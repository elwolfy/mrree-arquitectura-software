package pe.utec.consular.clean.citas.usecase.oficinas;

import pe.utec.consular.clean.citas.usecase.OficinaGateway;

import java.util.List;

/**
 * Interactor para listar el catálogo de oficinas consulares activas.
 * <p>Concepto: Clean Architecture – Interactor sin anotaciones de Spring (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code ListarOficinasService} en {@code monolito-modular}.
 */
public class ListarOficinasInteractor implements ListarOficinasInputBoundary {

    private final OficinaGateway oficinaGateway;
    private final ListarOficinasOutputBoundary outputBoundary;

    public ListarOficinasInteractor(OficinaGateway oficinaGateway, ListarOficinasOutputBoundary outputBoundary) {
        this.oficinaGateway = oficinaGateway;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void listar() {
        List<ListarOficinasResponseModel> respuesta = oficinaGateway.listarTodas().stream()
                .map(o -> new ListarOficinasResponseModel(
                        o.getId(),
                        o.getNombre(),
                        o.getPais(),
                        o.getCiudad(),
                        o.getZonaHoraria()
                ))
                .toList();
        outputBoundary.presentar(respuesta);
    }
}

