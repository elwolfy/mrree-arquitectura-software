package pe.utec.consular.tramites.application.port.out;

import java.util.Map;
import java.util.UUID;

/**
 * Puerto secundario para persistir la ficha documental JSONB del expediente ({@code tramites.expediente}).
 */
public interface ExpedienteRepositoryPort {

    void guardarExpediente(UUID tramiteId, Map<String, Object> metadatos);
}

