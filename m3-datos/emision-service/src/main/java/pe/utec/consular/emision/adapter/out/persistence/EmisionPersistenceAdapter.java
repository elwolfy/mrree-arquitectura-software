package pe.utec.consular.emision.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.emision.application.port.out.EmisionPersistencePort;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador JPA para {@link EmisionPersistencePort}.
 */
@Component
public class EmisionPersistenceAdapter implements EmisionPersistencePort {

    private final SpringDataDocumentoEmitidoRepository documentoRepository;
    private final SpringDataMensajeProcesadoRepository mensajeProcesadoRepository;

    public EmisionPersistenceAdapter(SpringDataDocumentoEmitidoRepository documentoRepository,
                                     SpringDataMensajeProcesadoRepository mensajeProcesadoRepository) {
        this.documentoRepository = documentoRepository;
        this.mensajeProcesadoRepository = mensajeProcesadoRepository;
    }

    @Override
    public boolean yaFueProcesado(String messageId) {
        return mensajeProcesadoRepository.existsById(messageId);
    }

    @Override
    public void registrarMensajeProcesado(String messageId) {
        mensajeProcesadoRepository.saveAndFlush(new MensajeProcesadoJpaEntity(messageId, Instant.now()));
    }

    @Override
    public long siguienteSecuenciaDocumento() {
        Long seq = documentoRepository.obtenerSiguienteSecuencia();
        return seq != null ? seq : 1L;
    }

    @Override
    public void guardarDocumentoEmitido(UUID tramiteId, String numeroDocumento) {
        documentoRepository.saveAndFlush(new DocumentoEmitidoJpaEntity(
                UUID.randomUUID(),
                tramiteId,
                numeroDocumento,
                Instant.now()
        ));
    }
}

