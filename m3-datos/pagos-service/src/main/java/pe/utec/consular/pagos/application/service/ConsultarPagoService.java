package pe.utec.consular.pagos.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.pagos.application.port.in.ConsultarPagoUseCase;
import pe.utec.consular.pagos.application.port.out.PagoRepositoryPort;
import pe.utec.consular.pagos.domain.OrdenDePago;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para consultar órdenes de pago registradas en {@code pagos.pago}.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarPagoService implements ConsultarPagoUseCase {

    private final PagoRepositoryPort pagoRepositoryPort;

    public ConsultarPagoService(PagoRepositoryPort pagoRepositoryPort) {
        this.pagoRepositoryPort = pagoRepositoryPort;
    }

    @Override
    public Optional<OrdenDePago> consultarPorId(UUID pagoId) {
        return pagoRepositoryPort.buscarPorId(pagoId);
    }

    @Override
    public List<OrdenDePago> listarPorTramiteId(UUID tramiteId) {
        return pagoRepositoryPort.buscarPorTramiteId(tramiteId);
    }
}

