package pe.utec.consular.pagos.application.port.out;

import java.util.UUID;

/**
 * Puerto secundario para registrar eventos de integración en {@code pagos.outbox}.
 */
public interface OutboxPort {

    void registrarEnOutbox(UUID agregadoId, String tipoEvento, Object cargaUtil);
}

