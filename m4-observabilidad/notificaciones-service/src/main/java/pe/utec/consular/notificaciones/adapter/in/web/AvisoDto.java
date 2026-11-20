package pe.utec.consular.notificaciones.adapter.in.web;

import pe.utec.consular.notificaciones.domain.Aviso;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida de un aviso consular.
 */
public record AvisoDto(
        UUID id,
        String dni,
        String destinatario,
        String asunto,
        String cuerpo,
        String canal,
        String estado,
        Instant creadoEn,
        String referencia
) {
    public static AvisoDto desdeDominio(Aviso a) {
        return new AvisoDto(
                a.id(),
                a.dni(),
                a.destinatario(),
                a.asunto(),
                a.cuerpo(),
                a.canal(),
                a.estado(),
                a.creadoEn(),
                a.referencia()
        );
    }
}

