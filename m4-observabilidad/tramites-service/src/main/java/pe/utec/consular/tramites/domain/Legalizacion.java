package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Subtipo de trámite consular para la Legalización diplomática de firmas (cadena consular).
 * <p>Tarifa base de laboratorio: 20.00 PEN (+10 % fuera de Sudamérica), hasta 50 folios.
 */
public final class Legalizacion extends Tramite {

    public static final String SUBTIPO = "Legalizacion";
    private static final BigDecimal TARIFA_BASE = new BigDecimal("20.00");

    public Legalizacion(UUID id,
                        CodigoTramite codigo,
                        Dni dni,
                        String nombres,
                        Email email,
                        String pais,
                        int folios,
                        String idioma,
                        EstadoTramite estado,
                        BigDecimal monto,
                        Instant creadoEn) {
        super(id, codigo, dni, nombres, email, TipoTramite.LEGALIZACION, pais, folios, idioma, estado, monto, creadoEn);
        if (folios > 50) {
            throw new SolicitudInvalidaException("Una Legalización no puede superar los 50 folios");
        }
    }

    public static Legalizacion crearNueva(CodigoTramite codigo, Solicitud solicitud) {
        BigDecimal monto = aplicarRecargoExteriorSiCorresponde(TARIFA_BASE, solicitud.getPais());
        return new Legalizacion(
                UUID.randomUUID(),
                codigo,
                solicitud.getDni(),
                solicitud.getNombres(),
                solicitud.getEmail(),
                solicitud.getPais(),
                solicitud.getFolios(),
                solicitud.getIdioma(),
                EstadoTramite.REGISTRADO,
                monto,
                Instant.now()
        );
    }

    @Override
    public String getSubtipo() {
        return SUBTIPO;
    }

    @Override
    public String getModalidadProcesamiento() {
        return "CADENA_FIRMAS_CONSULARES";
    }

    @Override
    public Map<String, Object> construirMetadatosExpediente() {
        Map<String, Object> metadatos = new LinkedHashMap<>();
        metadatos.put("folios", getFolios());
        metadatos.put("idioma", getIdioma());
        return metadatos;
    }
}
