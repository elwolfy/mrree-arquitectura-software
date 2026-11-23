package pe.utec.consular.emision.application.port.out;

import java.util.UUID;

/**
 * Puerto secundario de persistencia e idempotencia para {@code emision-service}.
 */
public interface EmisionPersistencePort {

    boolean yaFueProcesado(String messageId);

    void registrarMensajeProcesado(String messageId);

    long siguienteSecuenciaDocumento();

    void guardarDocumentoEmitido(UUID tramiteId, String numeroDocumento);
}

