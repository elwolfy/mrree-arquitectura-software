package pe.utec.consular.citas.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.citas.application.port.in.ConsultarCitaUseCase;
import pe.utec.consular.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.citas.domain.Cita;
import pe.utec.consular.citas.domain.Dni;
import pe.utec.consular.citas.domain.exception.RecursoNoEncontradoException;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación para consultar citas por ID o por DNI del ciudadano.
 */
@Service
public class ConsultarCitaService implements ConsultarCitaUseCase {

    private final CitaRepositoryPort citaRepositoryPort;

    public ConsultarCitaService(CitaRepositoryPort citaRepositoryPort) {
        this.citaRepositoryPort = citaRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Cita buscarPorId(UUID citaId) {
        return citaRepositoryPort.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cita con identificador: " + citaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> buscarPorDni(String dni) {
        Dni dniValidado = new Dni(dni);
        return citaRepositoryPort.buscarPorDni(dniValidado.valor());
    }
}

