package pe.utec.consular.serverless.funciones;

import java.time.LocalDate;
import java.util.List;
import pe.utec.consular.serverless.dominio.Disponibilidad;

public record DisponibilidadDto(
        String oficinaId,
        LocalDate fecha,
        String modoConsistencia,
        List<CupoDisponibilidadDto> cupos
) {
    public static DisponibilidadDto desdeDominio(Disponibilidad disp) {
        return new DisponibilidadDto(
                disp.oficinaId(),
                disp.fecha(),
                disp.modoConsistencia(),
                disp.cupos().stream().map(CupoDisponibilidadDto::desdeDominio).toList()
        );
    }
}

