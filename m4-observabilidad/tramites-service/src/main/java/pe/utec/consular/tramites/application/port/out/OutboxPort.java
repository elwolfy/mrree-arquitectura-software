package pe.utec.consular.tramites.application.port.out;

import java.util.UUID;

/**
 * Puerto secundario para registrar mensajes de integración en la tabla {@code tramites.outbox}
 * dentro de la misma transacción local que persiste los eventos de dominio.
 */
public interface OutboxPort {

    void registrarEnOutbox(UUID agregadoId, String tipoEvento, Object cargaUtil);
}

