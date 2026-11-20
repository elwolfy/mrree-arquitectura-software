package pe.utec.consular.tramites.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de lectura CQRS proyectado en {@code tramites.vista_tramite}.
 * <p>Todas las consultas por identificador ({@code GET /api/tramites/{id}}), por DNI
 * ({@code GET /api/tramites?dni=}) y por gRPC leen exclusivamente de esta proyección.
 */
public record VistaTramite(
        UUID tramiteId,
        String codigo,
        String dni,
        String nombres,
        String email,
        TipoTramite tipoTramite,
        String subtipo,
        String modalidadProcesamiento,
        String pais,
        int folios,
        String idioma,
        EstadoTramite estado,
        BigDecimal monto,
        String motivoRechazo,
        UUID pagoId,
        String numeroDocumento,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static String resolverSubtipo(TipoTramite tipo, String subtipoGuardado) {
        if (subtipoGuardado != null && !subtipoGuardado.isBlank()) {
            return subtipoGuardado;
        }
        return switch (tipo) {
            case LEGALIZACION -> Legalizacion.SUBTIPO;
            case APOSTILLA -> Apostilla.SUBTIPO;
            case REGISTRO_CONSULAR -> RegistroConsular.SUBTIPO;
        };
    }

    public static String resolverModalidad(TipoTramite tipo) {
        return switch (tipo) {
            case LEGALIZACION -> "CADENA_FIRMAS_CONSULARES";
            case APOSTILLA -> "SELLO_UNICO_CONVENIO_LA_HAYA";
            case REGISTRO_CONSULAR -> "ASIENTO_REGISTRAL_CONSULAR";
        };
    }
}

