package pe.utec.consular.notificaciones.application.port.out;

import java.util.UUID;

/**
 * Puerto de salida para el control de idempotencia de mensajes consumidos desde RabbitMQ.
 */
public interface MensajeProcesadoPort {

    boolean yaFueProcesado(UUID messageId);

    void registrarProcesado(UUID messageId, String tipoEvento);
}

