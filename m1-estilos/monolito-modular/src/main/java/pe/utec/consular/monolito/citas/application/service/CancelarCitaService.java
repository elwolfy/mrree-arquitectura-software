package pe.utec.consular.monolito.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.monolito.citas.application.port.in.CancelarCitaUseCase;
import pe.utec.consular.monolito.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.monolito.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cita;
import pe.utec.consular.monolito.compartido.domain.exception.RecursoNoEncontradoException;

import java.util.UUID;

/**
 * Caso de uso para cancelar una cita consular y liberar la franja del cupo asociado.
 * <p>Concepto: Transición de estados de dominio y consistencia transaccional (sección 1.3).
 */
@Service
public class CancelarCitaService implements CancelarCitaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelarCitaService.class);

    private final CitaRepositoryPort citaRepositoryPort;
    private final CupoRepositoryPort cupoRepositoryPort;

    public CancelarCitaService(CitaRepositoryPort citaRepositoryPort, CupoRepositoryPort cupoRepositoryPort) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.cupoRepositoryPort = cupoRepositoryPort;
    }

    @Override
    @Transactional
    public void cancelar(UUID id) {
        Cita cita = citaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cita con identificador: " + id));

        cita.cancelar();
        citaRepositoryPort.guardar(cita);

        // Liberar el cupo asociado
        cupoRepositoryPort.buscarPorOficinaFechaHoraConBloqueo(cita.getOficinaId(), cita.getFecha(), cita.getHora())
                .ifPresent(cupo -> {
                    cupo.liberar();
                    cupoRepositoryPort.guardar(cupo);
                });

        log.info("Cita {} cancelada y cupo liberado", cita.getCodigo().valor());
    }
}
