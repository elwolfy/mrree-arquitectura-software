package pe.utec.consular.tramites.domain.evento;

import pe.utec.consular.tramites.domain.TipoTramite;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Contrato sellado para los eventos de dominio inmutables que conforman el flujo de
 * <b>Event Sourcing</b> del agregado {@link pe.utec.consular.tramites.domain.Tramite}.
 * <p>Concepto: En Event Sourcing la fuente de la verdad no es una fila sobrescrita en base de datos,
 * sino la secuencia ordenada de eventos pasados almacenada en {@code tramites.eventos_tramite}.
 * <p>Sesión: S4 y S5.
 */
public sealed interface EventoDominioTramite
        permits TramiteRegistrado,
                PagoAsociado,
                EmisionSolicitada,
                TramiteEmitido,
                EmisionConError,
                TramiteRechazado,
                PagoAnulacionSolicitada {

    UUID tramiteId();

    String tipo();

    Instant ocurridoEn();

    Map<String, Object> aMapaDatos();

    /**
     * Reconstruye la instancia tipada de {@link EventoDominioTramite} a partir de una fila
     * almacenada en {@code tramites.eventos_tramite}.
     */
    static EventoDominioTramite desdeRegistro(UUID tramiteId, String tipo, Map<String, Object> datos, Instant ocurridoEn) {
        return switch (tipo) {
            case TramiteRegistrado.TIPO -> new TramiteRegistrado(
                    tramiteId,
                    String.valueOf(datos.get("codigo")),
                    String.valueOf(datos.get("dni")),
                    String.valueOf(datos.getOrDefault("nombres", "")),
                    String.valueOf(datos.getOrDefault("email", "")),
                    TipoTramite.valueOf(String.valueOf(datos.get("tipoTramite"))),
                    String.valueOf(datos.getOrDefault("pais", "PE")),
                    datos.get("folios") instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(datos.getOrDefault("folios", "1"))),
                    String.valueOf(datos.getOrDefault("idioma", "ES")),
                    new BigDecimal(String.valueOf(datos.get("monto"))),
                    ocurridoEn
            );
            case PagoAsociado.TIPO -> new PagoAsociado(
                    tramiteId,
                    UUID.fromString(String.valueOf(datos.get("pagoId"))),
                    new BigDecimal(String.valueOf(datos.get("monto"))),
                    ocurridoEn
            );
            case EmisionSolicitada.TIPO -> new EmisionSolicitada(
                    tramiteId,
                    ocurridoEn
            );
            case TramiteEmitido.TIPO -> new TramiteEmitido(
                    tramiteId,
                    String.valueOf(datos.get("numeroDocumento")),
                    ocurridoEn
            );
            case EmisionConError.TIPO -> new EmisionConError(
                    tramiteId,
                    String.valueOf(datos.get("motivo")),
                    ocurridoEn
            );
            case TramiteRechazado.TIPO -> new TramiteRechazado(
                    tramiteId,
                    String.valueOf(datos.get("motivo")),
                    ocurridoEn
            );
            case PagoAnulacionSolicitada.TIPO -> new PagoAnulacionSolicitada(
                    tramiteId,
                    datos.get("pagoId") != null ? UUID.fromString(String.valueOf(datos.get("pagoId"))) : null,
                    String.valueOf(datos.get("motivo")),
                    ocurridoEn
            );
            default -> throw new IllegalArgumentException("Tipo de evento de dominio desconocido en eventos_tramite: " + tipo);
        };
    }
}

