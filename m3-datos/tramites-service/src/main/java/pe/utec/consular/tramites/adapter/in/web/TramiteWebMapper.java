package pe.utec.consular.tramites.adapter.in.web;

import org.springframework.stereotype.Component;
import pe.utec.consular.tramites.application.port.in.RegistrarTramiteCommand;
import pe.utec.consular.tramites.domain.Tramite;
import pe.utec.consular.tramites.domain.VistaTramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;

import java.util.List;

/**
 * Mapeador entre el modelo de dominio / proyección CQRS de trámites y los DTOs de la capa web.
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
        return aDto(tramite.aVista());
    }

    public TramiteDto aDto(VistaTramite vista) {
        return new TramiteDto(
                vista.tramiteId(),
                vista.codigo(),
                vista.dni(),
                vista.nombres(),
                vista.email(),
                vista.tipoTramite(),
                vista.subtipo(),
                vista.modalidadProcesamiento(),
                vista.pais(),
                vista.folios(),
                vista.idioma(),
                vista.estado(),
                vista.monto(),
                vista.motivoRechazo(),
                vista.pagoId(),
                vista.numeroDocumento(),
                vista.creadoEn(),
                vista.actualizadoEn()
        );
    }

    public List<TramiteDto> aListaDto(List<VistaTramite> vistas) {
        return vistas.stream().map(this::aDto).toList();
    }

    public EventoHistorialDto aEventoHistorialDto(EventoVersionadoTramite eventoVersionado) {
        return new EventoHistorialDto(
                eventoVersionado.evento().tipo(),
                eventoVersionado.version(),
                eventoVersionado.evento().ocurridoEn(),
                eventoVersionado.evento().ocurridoEn(),
                eventoVersionado.evento().aMapaDatos()
        );
    }

    public List<EventoHistorialDto> aListaHistorialDto(List<EventoVersionadoTramite> eventos) {
        return eventos.stream().map(this::aEventoHistorialDto).toList();
    }
}
