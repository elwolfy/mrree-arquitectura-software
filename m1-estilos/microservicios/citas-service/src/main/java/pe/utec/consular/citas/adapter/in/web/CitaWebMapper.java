package pe.utec.consular.citas.adapter.in.web;

import pe.utec.consular.citas.application.port.in.RegistrarCitaCommand;
import pe.utec.consular.citas.domain.Cita;
import pe.utec.consular.citas.domain.Disponibilidad;
import pe.utec.consular.citas.domain.OficinaConsular;

import java.util.List;

/**
 * Mapeador entre objetos de dominio y DTOs web en el adaptador primario.
 */
public final class CitaWebMapper {

    private CitaWebMapper() {
    }

    public static RegistrarCitaCommand aComando(RegistrarCitaRequest req) {
        return new RegistrarCitaCommand(
                req.dni(),
                req.nombres(),
                req.email(),
                req.oficinaId(),
                req.fecha(),
                req.hora(),
                req.tipoTramite()
        );
    }

    public static CitaDto aDto(Cita c) {
        return new CitaDto(
                c.getId(),
                c.getCodigo().valor(),
                c.getDni().valor(),
                c.getNombres(),
                c.getOficinaId(),
                c.getFecha(),
                c.getHora(),
                c.getTipoTramite().name(),
                c.getEstado().name(),
                c.getNotificacion(),
                c.getCreadaEn()
        );
    }

    public static OficinaDto aDto(OficinaConsular o) {
        return new OficinaDto(o.getId(), o.getNombre(), o.getPais(), o.getCiudad(), o.getZonaHoraria());
    }

    public static DisponibilidadDto aDto(Disponibilidad d) {
        List<CupoDisponibilidadDto> items = d.cupos().stream()
                .map(c -> new CupoDisponibilidadDto(c.hora(), c.capacidad(), c.ocupados(), c.libres()))
                .toList();
        return new DisponibilidadDto(d.oficinaId(), d.fecha(), items, d.origen(), d.actualizadoHaceSegundos());
    }
}

