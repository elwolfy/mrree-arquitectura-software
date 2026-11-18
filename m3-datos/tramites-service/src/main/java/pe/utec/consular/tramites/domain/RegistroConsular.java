package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Subtipo de trámite consular para el Registro Consular (inscripción de nacionales y actas en el exterior).
 * <p>Requiere exactamente 1 folio e idioma español ({@code ES}). Tarifa base de laboratorio: 25.00 PEN (+10 % fuera de Sudamérica).
 */
public final class RegistroConsular extends Tramite {

    public static final String SUBTIPO = "RegistroConsular";
    private static final BigDecimal TARIFA_BASE = new BigDecimal("25.00");

    public RegistroConsular(UUID id,
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
        super(id, codigo, dni, nombres, email, TipoTramite.REGISTRO_CONSULAR, pais, folios, idioma, estado, monto, creadoEn);
        if (folios != 1) {
            throw new SolicitudInvalidaException("El Registro Consular admite únicamente 1 folio");
        }
        if (!"ES".equalsIgnoreCase(idioma)) {
            throw new SolicitudInvalidaException("El Registro Consular exige idioma español (ES)");
        }
    }

    public static RegistroConsular crearNuevo(CodigoTramite codigo, Solicitud solicitud) {
        BigDecimal monto = aplicarRecargoExteriorSiCorresponde(TARIFA_BASE, solicitud.getPais());
        return new RegistroConsular(
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
        return "ASIENTO_REGISTRAL_CONSULAR";
    }

    @Override
    public Map<String, Object> construirMetadatosExpediente() {
        Map<String, Object> metadatos = new LinkedHashMap<>();
        metadatos.put("paisDestino", getPais());
        metadatos.put("idioma", getIdioma());
        return metadatos;
    }
}
