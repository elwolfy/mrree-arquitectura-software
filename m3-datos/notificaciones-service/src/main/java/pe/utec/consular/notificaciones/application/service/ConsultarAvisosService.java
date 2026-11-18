package pe.utec.consular.notificaciones.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.notificaciones.application.port.in.ConsultarAvisosUseCase;
import pe.utec.consular.notificaciones.application.port.out.AvisoRepositoryPort;
import pe.utec.consular.notificaciones.domain.Aviso;

import java.util.List;

/**
 * Caso de uso para listar los avisos enviados a un ciudadano por DNI.
 */
@Service
public class ConsultarAvisosService implements ConsultarAvisosUseCase {

    private final AvisoRepositoryPort avisoRepositoryPort;

    public ConsultarAvisosService(AvisoRepositoryPort avisoRepositoryPort) {
        this.avisoRepositoryPort = avisoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aviso> listarPorDni(String dni) {
        return avisoRepositoryPort.buscarPorDni(dni);
    }
}

