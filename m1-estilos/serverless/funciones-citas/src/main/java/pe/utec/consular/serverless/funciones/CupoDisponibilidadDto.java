package pe.utec.consular.serverless.funciones;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.UUID;
import pe.utec.consular.serverless.dominio.Cupo;

public record CupoDisponibilidadDto(
        UUID cupoId,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        int capacidad,
        int disponibles
) {
    public static CupoDisponibilidadDto desdeDominio(Cupo cupo) {
        return new CupoDisponibilidadDto(
                cupo.getId(),
                cupo.getHora(),
                cupo.getCapacidad(),
                cupo.disponibles()
        );
    }
}

