package pe.utec.consular.monolito.citas.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.monolito.citas.application.port.in.ConsultarCitaUseCase;
import pe.utec.consular.monolito.citas.application.port.out.CitaRepositoryPort;
import pe.utec.consular.monolito.citas.domain.Cita;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para consultar citas consulares por identificador o DNI.
 * <p>Concepto: Arquitectura hexagonal – caso de uso (sección 1.3).
 */
@Service
@Transactional(readOnly = true)
public class ConsultarCitaService implements ConsultarCitaUseCase {

    private final CitaRepositoryPort citaRepositoryPort;

    public ConsultarCitaService(CitaRepositoryPort citaRepositoryPort) {
        this.citaRepositoryPort = citaRepositoryPort;
    }

    @Override
    public Optional<Cita> consultarPorId(UUID id) {
        return citaRepositoryPort.buscarPorId(id);
    }

    @Override
    public List<Cita> consultarPorDni(String dni) {
        return citaRepositoryPort.buscarPorDni(dni);
    }
}
