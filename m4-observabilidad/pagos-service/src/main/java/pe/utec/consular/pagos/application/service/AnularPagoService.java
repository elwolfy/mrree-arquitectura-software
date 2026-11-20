package pe.utec.consular.pagos.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.pagos.application.port.in.AnularPagoCommand;
import pe.utec.consular.pagos.application.port.in.AnularPagoUseCase;
import pe.utec.consular.pagos.application.port.out.OutboxPort;
import pe.utec.consular.pagos.application.port.out.PagoRepositoryPort;
import pe.utec.consular.pagos.domain.EstadoPago;
import pe.utec.consular.pagos.domain.OrdenDePago;
import pe.utec.consular.pagos.domain.PagoAnulado;
import pe.utec.consular.pagos.domain.exception.RecursoNoEncontradoException;
import pe.utec.consular.pagos.domain.exception.ReglaNegocioException;

/**
 * Caso de uso para anular un pago confirmado y publicar {@code pago.anulado} vía Outbox
 * (Andamiaje de la Asignación A3: acción compensatoria del paso de pago en la saga).
 */
@Service
public class AnularPagoService implements AnularPagoUseCase {

    private static final Logger log = LoggerFactory.getLogger(AnularPagoService.class);
    private static final String ROUTING_KEY_PAGO_ANULADO = "pago.anulado";

    private final PagoRepositoryPort pagoRepositoryPort;
    private final OutboxPort outboxPort;

    public AnularPagoService(PagoRepositoryPort pagoRepositoryPort,
                             OutboxPort outboxPort) {
        this.pagoRepositoryPort = pagoRepositoryPort;
        this.outboxPort = outboxPort;
    }

    @Override
    @Transactional
    public OrdenDePago anularPago(AnularPagoCommand comando) {
        if (comando.motivo() == null || comando.motivo().isBlank()) {
            throw new ReglaNegocioException("El motivo de anulación del pago es obligatorio");
        }

        OrdenDePago orden = pagoRepositoryPort.buscarPorId(comando.pagoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pago con identificador: " + comando.pagoId()));

        if (orden.getEstado() == EstadoPago.ANULADO) {
            log.info("El pago {} ya se encontraba en estado ANULADO; operación idempotente", orden.getId());
            return orden;
        }

        orden.anular();
        OrdenDePago guardada = pagoRepositoryPort.guardar(orden);

        outboxPort.registrarEnOutbox(
                guardada.getId(),
                ROUTING_KEY_PAGO_ANULADO,
                new PagoAnulado(guardada.getId(), guardada.getTramiteId(), comando.motivo().trim())
        );

        log.info("Pago {} del trámite {} pasado a estado ANULADO por motivo '{}' (outbox=pendiente)",
                guardada.getId(), guardada.getTramiteId(), comando.motivo().trim());

        return guardada;
    }
}

