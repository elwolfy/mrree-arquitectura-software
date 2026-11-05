package pe.utec.consular.monolito.citas.adapter.in.web;

import org.springframework.stereotype.Component;
import pe.utec.consular.monolito.citas.application.port.in.RegistrarCitaCommand;
import pe.utec.consular.monolito.citas.domain.Cita;
import pe.utec.consular.monolito.citas.domain.Disponibilidad;
import pe.utec.consular.monolito.citas.domain.OficinaConsular;

import java.util.List;

/**
 * Mapeador entre modelos de dominio y DTOs de la capa web.
 * <p>Concepto: Separación de modelos en arquitectura hexagonal (regla 01).
 */
@Component
public class CitaWebMapper {

    public RegistrarCitaCommand aComando(RegistrarCitaRequest request) {
        return new RegistrarCitaCommand(
                request.dni(),
                request.nombres(),
                request.email(),
                request.oficinaId(),
                request.fecha(),
                request.hora(),
                request.tipoTramite()
        );
    }

    public CitaDto aDto(Cita cita) {
        return new CitaDto(
                cita.getId(),
                cita.getCodigo().valor(),
                cita.getDni().valor(),
                cita.getNombres(),
                cita.getOficinaId(),
                cita.getFecha(),
                cita.getHora(),
                cita.getTipoTramite(),
                cita.getEstado(),
                cita.getNotificacion(),
                cita.getCreadaEn()
        );
    }

    public List<CitaDto> aListaCitaDto(List<Cita> citas) {
        return citas.stream().map(this::aDto).toList();
    }

    public OficinaDto aDto(OficinaConsular oficina) {
        return new OficinaDto(
                oficina.getId(),
                oficina.getNombre(),
                oficina.getPais(),
                oficina.getCiudad(),
                oficina.getZonaHoraria()
        );
    }

    public List<OficinaDto> aListaOficinaDto(List<OficinaConsular> oficinas) {
        return oficinas.stream().map(this::aDto).toList();
    }

    public DisponibilidadDto aDto(Disponibilidad disponibilidad) {
        List<CupoDisponibilidadDto> cuposDto = disponibilidad.cupos().stream()
                .map(c -> new CupoDisponibilidadDto(c.hora(), c.capacidad(), c.ocupados(), c.libres()))
                .toList();

        return new DisponibilidadDto(
                disponibilidad.oficinaId(),
                disponibilidad.fecha(),
                cuposDto,
                disponibilidad.origen(),
                disponibilidad.actualizadoHaceSegundos()
        );
    }
}
