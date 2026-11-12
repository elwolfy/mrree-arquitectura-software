package pe.utec.consular.notificaciones.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.utec.consular.notificaciones.domain.Aviso;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta que representa un aviso procesado por {@code notificaciones-service}.
 */
@Schema(description = "Aviso consular registrado")
public record AvisoDto(
        @Schema(description = "Identificador único del aviso")
        UUID id,
        @Schema(description = "Correo destinatario", example = "ana@correo.lab")
        String destinatario,
        @Schema(description = "Asunto del aviso", example = "Confirmación de Cita Consular - CIT-2026-000001")
        String asunto,
        @Schema(description = "Cuerpo del mensaje")
        String cuerpo,
        @Schema(description = "Canal utilizado", example = "CONSOLA")
        String canal,
        @Schema(description = "Estado del envío", example = "ENVIADA")
        String estado,
        @Schema(description = "Instante de creación ISO-8601")
        Instant creadoEn,
        @Schema(description = "Referencia asociada", example = "CIT-2026-000001")
        String referencia
) {
    public static AvisoDto desdeDominio(Aviso aviso) {
        return new AvisoDto(
                aviso.getId(),
                aviso.getDestinatario(),
                aviso.getAsunto(),
                aviso.getCuerpo(),
                aviso.getCanal().name(),
                aviso.getEstado().name(),
                aviso.getCreadoEn(),
                aviso.getReferencia()
        );
    }
}

