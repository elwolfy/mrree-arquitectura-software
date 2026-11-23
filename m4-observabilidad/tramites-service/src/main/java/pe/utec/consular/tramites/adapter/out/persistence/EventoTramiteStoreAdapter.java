package pe.utec.consular.tramites.adapter.out.persistence;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.out.EventoTramiteStorePort;
import pe.utec.consular.tramites.domain.evento.EventoDominioTramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;
import pe.utec.consular.tramites.domain.exception.ConflictoVersionException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador JPA para el Event Store {@code tramites.eventos_tramite}.
 * <p>Aplica control de concurrencia optimista verificando que la versión actual en base de datos
 * coincida con {@code versionEsperada} antes de insertar los nuevos eventos y traduciendo
 * cualquier violación de la restricción única {@code (tramite_id, version)} a {@link ConflictoVersionException}.
 */
@Component
public class EventoTramiteStoreAdapter implements EventoTramiteStorePort {

    private final SpringDataEventoTramiteRepository repository;

    public EventoTramiteStoreAdapter(SpringDataEventoTramiteRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardarEventos(UUID tramiteId, int versionEsperada, List<EventoVersionadoTramite> nuevosEventos) {
        if (nuevosEventos == null || nuevosEventos.isEmpty()) {
            return;
        }
        int versionActualEnBd = repository.obtenerUltimaVersion(tramiteId);
        if (versionActualEnBd != versionEsperada) {
            throw new ConflictoVersionException(String.format(
                    "Conflicto de concurrencia optimista para trámite %s: versión esperada %d pero en eventos_tramite existe versión %d",
                    tramiteId, versionEsperada, versionActualEnBd));
        }

        int siguienteVersionEsperada = versionEsperada + 1;
        List<EventoTramiteJpaEntity> entidades = new ArrayList<>(nuevosEventos.size());
        for (EventoVersionadoTramite ev : nuevosEventos) {
            if (ev.version() != siguienteVersionEsperada) {
                throw new ConflictoVersionException(String.format(
                        "Secuencia de versión inválida al guardar evento %s del trámite %s: se esperaba %d pero fue %d",
                        ev.evento().tipo(), tramiteId, siguienteVersionEsperada, ev.version()));
            }
            entidades.add(new EventoTramiteJpaEntity(
                    tramiteId,
                    ev.version(),
                    ev.evento().tipo(),
                    ev.evento().aMapaDatos(),
                    ev.evento().ocurridoEn()
            ));
            siguienteVersionEsperada++;
        }

        try {
            repository.saveAllAndFlush(entidades);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictoVersionException(String.format(
                    "Conflicto de concurrencia optimista en BD (UNIQUE tramite_id, version) para trámite %s a partir de versión %d",
                    tramiteId, versionEsperada + 1));
        }
    }

    @Override
    public List<EventoVersionadoTramite> obtenerEventosPorTramiteId(UUID tramiteId) {
        return repository.findByTramiteIdOrderByVersionAsc(tramiteId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Map<UUID, List<EventoVersionadoTramite>> obtenerTodosLosFlujosDeEventos() {
        List<EventoTramiteJpaEntity> filas = repository.listarTodosOrdenadosPorTramiteYVersion();
        Map<UUID, List<EventoVersionadoTramite>> flujos = new LinkedHashMap<>();
        for (EventoTramiteJpaEntity fila : filas) {
            flujos.computeIfAbsent(fila.getTramiteId(), k -> new ArrayList<>())
                    .add(aDominio(fila));
        }
        return flujos;
    }

    @Override
    public long siguienteSecuenciaCodigo() {
        Long seq = repository.obtenerSiguienteSecuencia();
        return seq != null ? seq : 1L;
    }

    private EventoVersionadoTramite aDominio(EventoTramiteJpaEntity entity) {
        EventoDominioTramite evento = EventoDominioTramite.desdeRegistro(
                entity.getTramiteId(),
                entity.getTipo(),
                entity.getDatos(),
                entity.getOcurridoEn()
        );
        return new EventoVersionadoTramite(entity.getVersion(), evento);
    }
}

