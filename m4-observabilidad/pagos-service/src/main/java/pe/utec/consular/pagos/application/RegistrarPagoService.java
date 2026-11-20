package pe.utec.consular.pagos.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.pagos.application.port.in.RegistrarPagoCommand;
import pe.utec.consular.pagos.application.port.in.RegistrarPagoUseCase;
import pe.utec.consular.pagos.application.port.out.MetricasPagoPort;
import pe.utec.consular.pagos.application.port.out.OutboxPort;
import pe.utec.consular.pagos.application.port.out.PagoRepositoryPort;
import pe.utec.consular.pagos.domain.OrdenDePago;
import pe.utec.consular.pagos.domain.PagoConfirmado;

import java.util.Optional;

/**
 * Caso de uso para el registro y confirmación idempotente de pagos consulares.
 *
 * <h3>Idempotencia HTTP mediante la cabecera {@code Idempotency-Key} (IETF Internet-Draft)</h3>
 * <p>El método HTTP {@code POST} no es idempotente por definición en RFC 9110: si un cliente
 * sufre un corte de red o un <i>timeout</i> mientras aguarda la respuesta de {@code POST /api/pagos}
 * y reintenta la solicitud, un servidor sin control de idempotencia procesaría un segundo cargo.
 * <p>Siguiendo la especificación del grupo de trabajo HTTPAPI de la IETF
 * (<i>The Idempotency-Key HTTP Header Field</i>, {@code draft-ietf-httpapi-idempotency-key-header}),
 * el cliente genera un identificador único (UUIDv4) por intención de pago y lo envía en la cabecera
 * {@code Idempotency-Key}:
 * <ol>
 *   <li>Si la clave falta o está en blanco, el controlador rechaza la solicitud con {@code 400 Bad Request}
 *       ({@code ProblemDetail}).</li>
 *   <li>Si {@code pagos.pago} ya contiene un registro con esa {@code clave_idempotencia}
 *       (protegida además por {@code UNIQUE(clave_idempotencia)}), {@code RegistrarPagoService}
 *       devuelve exactamente el mismo pago confirmado con {@code 200 OK}, sin duplicar el cobro
 *       ni emitir un segundo evento {@code pago.confirmado}.</li>
 *   <li>Si la clave es nueva, se guarda la orden en {@code CONFIRMADO} y se inserta el evento
 *       {@code pago.confirmado} en {@code pagos.outbox} dentro de la misma transacción local ACID.</li>
 * </ol>
 * <p>Conceptos: Idempotencia en APIs de pago + Transactional Outbox (sección 3.3).
 * <p>Sesión: S4 y S5.
 */
@Service
public class RegistrarPagoService implements RegistrarPagoUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarPagoService.class);
    private static final String ROUTING_KEY_PAGO_CONFIRMADO = "pago.confirmado";

    private final PagoRepositoryPort pagoRepositoryPort;
    private final OutboxPort outboxPort;
    private final MetricasPagoPort metricasPagoPort;
    private final long latenciaInyectadaMs;

    public RegistrarPagoService(PagoRepositoryPort pagoRepositoryPort,
                                OutboxPort outboxPort,
                                MetricasPagoPort metricasPagoPort,
                                @Value("${latencia.inyectada.ms:0}") long latenciaInyectadaMs) {
        this.pagoRepositoryPort = pagoRepositoryPort;
        this.outboxPort = outboxPort;
        this.metricasPagoPort = metricasPagoPort;
        this.latenciaInyectadaMs = latenciaInyectadaMs;
    }

    @Override
    @Transactional
    public OrdenDePago registrarPago(RegistrarPagoCommand comando) {
        if (comando.claveIdempotencia() == null || comando.claveIdempotencia().isBlank()) {
            throw new IllegalArgumentException("La cabecera Idempotency-Key es obligatoria para procesar un pago");
        }

        aplicarLatenciaInyectadaSiCorresponde();

        String clave = comando.claveIdempotencia().trim();
        Optional<OrdenDePago> existente = pagoRepositoryPort.buscarPorClaveIdempotencia(clave);
        if (existente.isPresent()) {
            OrdenDePago pagoExistente = existente.get();
            log.info("Reintento idempotente detectado (Idempotency-Key={}): devolviendo pago existente {} para trámite {}",
                    clave, pagoExistente.getId(), pagoExistente.getTramiteId());
            return pagoExistente;
        }

        OrdenDePago orden = OrdenDePago.nuevaPendiente(comando.tramiteId(), comando.monto(), clave);
        orden.confirmar();

        OrdenDePago guardada = pagoRepositoryPort.guardar(orden);

        outboxPort.registrarEnOutbox(
                guardada.getId(),
                ROUTING_KEY_PAGO_CONFIRMADO,
                new PagoConfirmado(guardada.getId(), guardada.getTramiteId(), guardada.getMonto())
        );
        metricasPagoPort.incrementarPagosConfirmados();

        log.info("Pago {} confirmado para trámite {} por monto {} PEN (Idempotency-Key={}, outbox=pendiente)",
                guardada.getId(), guardada.getTramiteId(), guardada.getMonto(), clave);

        return guardada;
    }

    private void aplicarLatenciaInyectadaSiCorresponde() {
        if (latenciaInyectadaMs > 0) {
            log.info("Inyectando latencia artificial de {} ms en pagos-service", latenciaInyectadaMs);
            try {
                Thread.sleep(latenciaInyectadaMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

