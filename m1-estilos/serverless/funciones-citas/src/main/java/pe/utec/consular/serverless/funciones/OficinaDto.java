package pe.utec.consular.serverless.funciones;

import pe.utec.consular.serverless.dominio.OficinaConsular;

public record OficinaDto(
        String id,
        String nombre,
        String pais,
        String ciudad,
        String zonaHoraria
) {
    public static OficinaDto desdeDominio(OficinaConsular oficina) {
        return new OficinaDto(
                oficina.id(),
                oficina.nombre(),
                oficina.pais(),
                oficina.ciudad(),
                oficina.zonaHoraria()
        );
    }
}

