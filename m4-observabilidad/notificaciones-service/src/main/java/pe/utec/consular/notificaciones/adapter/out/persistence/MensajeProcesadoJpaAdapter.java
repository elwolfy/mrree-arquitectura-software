package pe.utec.consular.notificaciones.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.utec.consular.notificaciones.application.port.out.MensajeProcesadoPort;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador JPA para el control de idempotencia sobre {@code notificaciones.mensaje_procesado}.
 */
@Component
public class MensajeProcesadoJpaAdapter implements MensajeProcesadoPort {

    private final SpringDataMensajeProcesadoRepository repository;

    public MensajeProcesadoJpaAdapter(SpringDataMensajeProcesadoRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean yaFueProcesado(UUID messageId) {
        return repository.existsById(messageId);
    }

    @Override
    public void registrarProcesado(UUID messageId, String tipoEvento) {
        repository.save(new MensajeProcesadoJpaEntity(messageId, tipoEvento, Instant.now()));
    }
}

