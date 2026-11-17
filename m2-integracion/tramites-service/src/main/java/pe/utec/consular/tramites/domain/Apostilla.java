package pe.utec.consular.tramites.domain;

import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Subtipo de trámite consular para la Apostilla de La Haya (certificación única).
 * <p>Tarifa base de laboratorio: 30.00 PEN (+10 % fuera de Sudamérica), hasta 20 folios.
 */
public final class Apostilla extends Tramite {

    public static final String SUBTIPO = "Apostilla";
    private static final BigDecimal TARIFA_BASE = new BigDecimal("30.00");

    public Apostilla(UUID id,
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
        super(id, codigo, dni, nombres, email, TipoTramite.APOSTILLA, pais, folios, idioma, estado, monto, creadoEn);
        if (folios > 20) {
            throw new SolicitudInvalidaException("Una Apostilla no puede superar los 20 folios");
        }
    }

    public static Apostilla crearNueva(CodigoTramite codigo, Solicitud solicitud) {
        BigDecimal monto = aplicarRecargoExteriorSiCorresponde(TARIFA_BASE, solicitud.getPais());
        return new Apostilla(
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
        return "SELLO_UNICO_CONVENIO_LA_HAYA";
    }
}

