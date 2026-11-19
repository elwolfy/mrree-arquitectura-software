package pe.utec.consular.emision.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utec.consular.emision.application.port.in.ProcesarSolicitudEmisionCommand;
import pe.utec.consular.emision.application.port.in.ProcesarSolicitudEmisionUseCase;
import pe.utec.consular.emision.application.port.out.EmisionPersistencePort;
import pe.utec.consular.emision.application.port.out.PublicadorEventosEmisionPort;
import pe.utec.consular.emision.domain.EmisionCompletada;
import pe.utec.consular.emision.domain.EmisionFallida;

import java.time.Year;

/**
 * Caso de uso que procesa de forma idempotente la orden de emisión {@code emision.solicitar}.
 * <p>Simula el tiempo de generación y firmado electrónico del documento (500 ms por defecto) y:
 * <ul>
 *   <li>Si {@code emision.simular-falla=false}, genera el número {@code DOC-<año>-<secuencia 6 dígitos>},
 *       persiste el documento y publica {@code emision.completada}.</li>
 *   <li>Si {@code emision.simular-falla=true}, publica {@code emision.fallida} con el motivo del fallo
 *       para activar la rama de error de la saga en {@code tramites-service}.</li>
 * </ul>
 * <p>Conceptos: Consumidor idempotente ({@code emision.mensaje_procesado}) y participante de Saga Orquestada.
 * <p>Sesión: S4 y S5.
 */
@Service
public class ProcesarSolicitudEmisionService implements ProcesarSolicitudEmisionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcesarSolicitudEmisionService.class);

    private final EmisionPersistencePort persistencePort;
    private final PublicadorEventosEmisionPort publicadorPort;
    private volatile boolean simularFalla;
    private final long duracionMs;

    public ProcesarSolicitudEmisionService(EmisionPersistencePort persistencePort,
                                           PublicadorEventosEmisionPort publicadorPort,
                                           @Value("${emision.simular-falla:false}") boolean simularFalla,
                                           @Value("${emision.duracion-ms:500}") long duracionMs) {
        this.persistencePort = persistencePort;
        this.publicadorPort = publicadorPort;
        this.simularFalla = simularFalla;
        this.duracionMs = duracionMs;
    }

    public void setSimularFalla(boolean simularFalla) {
        this.simularFalla = simularFalla;
    }

    @Override
    @Transactional
    public void procesar(ProcesarSolicitudEmisionCommand comando) {
        if (persistencePort.yaFueProcesado(comando.messageId())) {
            log.info("Comando emision.solicitar duplicado ignorado por idempotencia (messageId={}, tramiteId={})",
                    comando.messageId(), comando.tramiteId());
            return;
        }

        simularGeneracionDocumento();
        persistencePort.registrarMensajeProcesado(comando.messageId());

        if (simularFalla) {
            String motivo = "Falla simulada en el servicio de firma digital consular (EMISION_SIMULAR_FALLA=true)";
            log.warn("Emisión fallida para trámite {}: {}", comando.tramiteId(), motivo);
            publicadorPort.publicarEmisionFallida(new EmisionFallida(comando.tramiteId(), motivo));
            return;
        }

        long secuencia = persistencePort.siguienteSecuenciaDocumento();
        String numeroDocumento = String.format("DOC-%04d-%06d", Year.now().getValue(), secuencia);
        persistencePort.guardarDocumentoEmitido(comando.tramiteId(), numeroDocumento);

        log.info("Documento consular {} emitido satisfactoriamente para trámite {}",
                numeroDocumento, comando.tramiteId());
        publicadorPort.publicarEmisionCompletada(new EmisionCompletada(comando.tramiteId(), numeroDocumento));
    }

    private void simularGeneracionDocumento() {
        if (duracionMs > 0) {
            try {
                Thread.sleep(duracionMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

