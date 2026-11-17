package pe.utec.consular.tramites.adapter.in.web;

import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteCommand;
import pe.utec.consular.tramites.domain.Tramite;

import java.util.List;

/**
 * Mapeador entre el modelo de dominio de trámites y los DTOs de la capa web.
 */
@Component
public class TramiteWebMapper {

    public RegistrarTramiteCommand aComando(RegistrarTramiteRequest request) {
        return new RegistrarTramiteCommand(
                request.dni(),
                request.nombres(),
                request.email(),
                request.tipoTramite(),
                request.pais(),
                request.folios(),
                request.idioma()
        );
    }

    public TramiteDto aDto(Tramite tramite) {
        return new TramiteDto(
                tramite.getId(),
                tramite.getCodigo().valor(),
                tramite.getDni().valor(),
                tramite.getNombres(),
                tramite.getEmail().valor(),
                tramite.getTipoTramite(),
                tramite.getSubtipo(),
                tramite.getModalidadProcesamiento(),
                tramite.getPais(),
                tramite.getFolios(),
                tramite.getIdioma(),
                tramite.getEstado(),
                tramite.getMonto(),
                tramite.getCreadoEn()
        );
    }

    public List<TramiteDto> aListaDto(List<Tramite> tramites) {
        return tramites.stream().map(this::aDto).toList();
    }
}

