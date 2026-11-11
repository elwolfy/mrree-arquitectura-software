package pe.utec.consular.clean.citas.usecase.consultar;

import pe.utec.consular.clean.citas.entities.Cita;
import pe.utec.consular.clean.citas.entities.exception.RecursoNoEncontradoException;
import pe.utec.consular.clean.citas.usecase.CitaGateway;

import java.util.List;
import java.util.UUID;

/**
 * Interactor para consultar citas por identificador o por DNI del ciudadano.
 * <p>Concepto: Clean Architecture – Interactor sin anotaciones de Spring (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code ConsultarCitaService} en {@code monolito-modular}.
 */
public class ConsultarCitaInteractor implements ConsultarCitaInputBoundary {

    private final CitaGateway citaGateway;
    private final ConsultarCitaOutputBoundary outputBoundary;

    public ConsultarCitaInteractor(CitaGateway citaGateway, ConsultarCitaOutputBoundary outputBoundary) {
        this.citaGateway = citaGateway;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void consultarPorId(UUID citaId) {
        Cita cita = citaGateway.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cita con identificador: " + citaId));
        outputBoundary.presentarCita(aResponseModel(cita));
    }

    @Override
    public void consultarPorDni(String dni) {
        List<ConsultarCitaResponseModel> lista = citaGateway.buscarPorDni(dni).stream()
                .map(this::aResponseModel)
                .toList();
        outputBoundary.presentarListaCitas(lista);
    }

    private ConsultarCitaResponseModel aResponseModel(Cita cita) {
        return new ConsultarCitaResponseModel(
                cita.getId(),
                cita.getCodigo().valor(),
                cita.getDni().valor(),
                cita.getNombres(),
                cita.getOficinaId(),
                cita.getFecha(),
                cita.getHora(),
                cita.getTipoTramite(),
                cita.getEstado(),
                cita.getNotificacion(),
                cita.getCreadaEn()
        );
    }
}

