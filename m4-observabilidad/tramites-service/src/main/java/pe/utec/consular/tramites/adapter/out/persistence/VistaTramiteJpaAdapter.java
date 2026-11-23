package pe.utec.consular.tramites.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.out.VistaTramiteRepositoryPort;
import pe.utec.consular.tramites.domain.EstadoTramite;
import pe.utec.consular.tramites.domain.TipoTramite;
import pe.utec.consular.tramites.domain.VistaTramite;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para la proyección de lectura CQRS {@code tramites.vista_tramite}.
 */
@Component
public class VistaTramiteJpaAdapter implements VistaTramiteRepositoryPort {

    private final SpringDataVistaTramiteRepository repository;
    private final ObjectMapper objectMapper;

    public VistaTramiteJpaAdapter(SpringDataVistaTramiteRepository repository,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public VistaTramite guardar(VistaTramite vista) {
        VistaTramiteJpaEntity entity = new VistaTramiteJpaEntity(
                vista.tramiteId(),
                vista.codigo(),
                vista.dni(),
                vista.tipoTramite().name(),
                vista.estado().name(),
                vista.monto(),
                vista.motivoRechazo(),
                vista.actualizadoEn(),
                vista.nombres(),
                vista.email(),
                vista.subtipo(),
                vista.pais(),
                vista.folios(),
                vista.idioma(),
                vista.pagoId(),
                vista.numeroDocumento(),
                vista.creadoEn()
        );
        VistaTramiteJpaEntity guardada = repository.saveAndFlush(entity);
        return aDominio(guardada);
    }

    @Override
    public Optional<VistaTramite> buscarPorId(UUID tramiteId) {
        return repository.findById(tramiteId).map(this::aDominio);
    }

    @Override
    public List<VistaTramite> buscarPorDni(String dni) {
        return repository.findByDniOrderByActualizadoEnDesc(dni).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<VistaTramite> buscarPorPaisDestinoEnExpediente(String paisDestino) {
        String filtroJson;
        try {
            filtroJson = objectMapper.writeValueAsString(Map.of("paisDestino", paisDestino.trim().toUpperCase()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el filtro JSONB de búsqueda por país", e);
        }
        return repository.buscarPorMetadatosJsonb(filtroJson).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public void eliminarTodas() {
        repository.eliminarTodasLasFilas();
    }

    private VistaTramite aDominio(VistaTramiteJpaEntity entity) {
        TipoTramite tipo = TipoTramite.valueOf(entity.getTipoTramite());
        return new VistaTramite(
                entity.getTramiteId(),
                entity.getCodigo(),
                entity.getDni(),
                entity.getNombres(),
                entity.getEmail(),
                tipo,
                VistaTramite.resolverSubtipo(tipo, entity.getSubtipo()),
                VistaTramite.resolverModalidad(tipo),
                entity.getPais(),
                entity.getFolios(),
                entity.getIdioma(),
                EstadoTramite.valueOf(entity.getEstado()),
                entity.getMonto(),
                entity.getMotivoRechazo(),
                entity.getPagoId(),
                entity.getNumeroDocumento(),
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}

