package pe.utec.consular.serverless.funciones;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import pe.utec.consular.serverless.dominio.Cita;
import pe.utec.consular.serverless.dominio.EstadoCita;
import pe.utec.consular.serverless.dominio.EstadoNotificacion;
import pe.utec.consular.serverless.dominio.TipoTramite;

public record CitaDto(
        UUID id,
        String codigo,
        String dni,
        String nombres,
        String email,
        String oficinaId,
        TipoTramite tipoTramite,
        LocalDate fecha,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        EstadoCita estado,
        EstadoNotificacion notificacion,
        Instant creadaEn
) {
    public static CitaDto desdeDominio(Cita cita) {
        return new CitaDto(
                cita.getId(),
                cita.getCodigo().valor(),
                cita.getDni().valor(),
                cita.getNombres(),
                cita.getEmail().valor(),
                cita.getOficinaId(),
                cita.getTipoTramite(),
                cita.getFecha(),
                cita.getHora(),
                cita.getEstado(),
                cita.getNotificacion(),
                cita.getCreadaEn()
        );
    }
}

