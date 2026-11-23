package pe.utec.consular.notificaciones.application.port.out;

import pe.utec.consular.notificaciones.domain.Aviso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de avisos.
 * <p>Concepto: Arquitectura hexagonal – puerto secundario (sección 1.3 del plan).
 */
public interface AvisoRepositoryPort {

    Aviso guardar(Aviso aviso);

    Optional<Aviso> buscarPorId(UUID id);

    List<Aviso> buscarPorReferencia(String referencia);

    List<Aviso> listarTodos();
}

