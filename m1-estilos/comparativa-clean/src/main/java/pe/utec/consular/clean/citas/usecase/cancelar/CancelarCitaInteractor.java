package pe.utec.consular.clean.citas.usecase.cancelar;

import pe.utec.consular.clean.citas.entities.Cita;
import pe.utec.consular.clean.citas.entities.exception.RecursoNoEncontradoException;
import pe.utec.consular.clean.citas.usecase.CitaGateway;
import pe.utec.consular.clean.citas.usecase.CupoGateway;

import java.util.UUID;

/**
 * Interactor para cancelar una cita consular y liberar su cupo asociado.
 * <p>Concepto: Clean Architecture – Interactor sin anotaciones de Spring (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code CancelarCitaService} en {@code monolito-modular}.
 */
public class CancelarCitaInteractor implements CancelarCitaInputBoundary {

    private final CitaGateway citaGateway;
    private final CupoGateway cupoGateway;
    private final CancelarCitaOutputBoundary outputBoundary;

    public CancelarCitaInteractor(CitaGateway citaGateway,
                                  CupoGateway cupoGateway,
                                  CancelarCitaOutputBoundary outputBoundary) {
        this.citaGateway = citaGateway;
        this.cupoGateway = cupoGateway;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void cancelar(UUID citaId) {
        Cita cita = citaGateway.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cita con identificador: " + citaId));

        cita.cancelar();
        citaGateway.guardar(cita);

        cupoGateway.buscarPorOficinaFechaHoraConBloqueo(cita.getOficinaId(), cita.getFecha(), cita.getHora())
                .ifPresent(cupo -> {
                    cupo.liberar();
                    cupoGateway.guardar(cupo);
                });

        outputBoundary.presentarCancelacion(new CancelarCitaResponseModel(
                cita.getId(),
                cita.getCodigo().valor(),
                cita.getEstado().name()
        ));
    }
}

