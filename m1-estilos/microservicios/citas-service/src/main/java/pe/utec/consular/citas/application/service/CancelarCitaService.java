package pe.utec.consular.citas.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.citas.application.port.in.CancelarCitaUseCase;
import pe.utec.consular.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.citas.application.port.out.CupoRepositoryPort;
import pe.utec.consular.citas.domain.Cita;
import pe.utec.consular.citas.domain.Cupo;
import pe.utec.consular.citas.domain.exception.RecursoNoEncontradoException;

import java.util.UUID;

/**
 * Servicio de aplicación para cancelar una cita activa y liberar su cupo horario.
 */
@Service
public class CancelarCitaService implements CancelarCitaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelarCitaService.class);

    private final CitaRepositoryPort citaRepositoryPort;
    private final CupoRepositoryPort cupoRepositoryPort;

    public CancelarCitaService(CitaRepositoryPort citaRepositoryPort,
                               CupoRepositoryPort cupoRepositoryPort) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.cupoRepositoryPort = cupoRepositoryPort;
    }

    @Override
    @Transactional
    public void cancelar(UUID citaId) {
        Cita cita = citaRepositoryPort.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cita con identificador: " + citaId));

        cita.cancelar();
        citaRepositoryPort.guardar(cita);

        Cupo cupo = cupoRepositoryPort.buscarPorOficinaFechaHoraConBloqueo(
                        cita.getOficinaId(), cita.getFecha(), cita.getHora())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el cupo asociado a la cita"));

        cupo.liberar();
        cupoRepositoryPort.guardar(cupo);

        log.info("Cita {} cancelada y cupo liberado en oficina {}", cita.getCodigo().valor(), cita.getOficinaId());
    }
}

