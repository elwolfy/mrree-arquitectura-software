package pe.utec.consular.tramites.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias de {@link TramiteFactory} (Patrón Factory Method).
 */
@DisplayName("TramiteFactory – Creación polimórfica de trámites consulares (Factory Method)")
class TramiteFactoryTest {

    @Test
    @DisplayName("Debe instanciar el subtipo Legalizacion cuando el tipo es LEGALIZACION")
    void debeCrearSubtipoLegalizacion() {
        Solicitud solicitud = Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.LEGALIZACION)
                .pais("ES")
                .folios(3)
                .idioma("ES")
                .build();

        Tramite tramite = TramiteFactory.crear(TipoTramite.LEGALIZACION, solicitud);

        assertInstanceOf(Legalizacion.class, tramite);
        assertEquals("Legalizacion", tramite.getSubtipo());
        assertEquals(EstadoTramite.REGISTRADO, tramite.getEstado());
        assertEquals(new BigDecimal("22.00"), tramite.getMonto());
    }

    @Test
    @DisplayName("Debe instanciar el subtipo Apostilla cuando el tipo es APOSTILLA")
    void debeCrearSubtipoApostilla() {
        Solicitud solicitud = Solicitud.builder()
                .dni("40000002")
                .tipo(TipoTramite.APOSTILLA)
                .pais("ES")
                .folios(2)
                .idioma("ES")
                .build();

        Tramite tramite = TramiteFactory.crear(TipoTramite.APOSTILLA, solicitud);

        assertInstanceOf(Apostilla.class, tramite);
        assertEquals("Apostilla", tramite.getSubtipo());
        assertEquals(EstadoTramite.REGISTRADO, tramite.getEstado());
        assertEquals(new BigDecimal("33.00"), tramite.getMonto());
    }

    @Test
    @DisplayName("Debe instanciar el subtipo RegistroConsular cuando el tipo es REGISTRO_CONSULAR")
    void debeCrearSubtipoRegistroConsular() {
        Solicitud solicitud = Solicitud.builder()
                .dni("40000003")
                .tipo(TipoTramite.REGISTRO_CONSULAR)
                .pais("AR")
                .folios(1)
                .idioma("ES")
                .build();

        Tramite tramite = TramiteFactory.crear(TipoTramite.REGISTRO_CONSULAR, solicitud);

        assertInstanceOf(RegistroConsular.class, tramite);
        assertEquals("RegistroConsular", tramite.getSubtipo());
        assertEquals(EstadoTramite.REGISTRADO, tramite.getEstado());
        assertEquals(new BigDecimal("25.00"), tramite.getMonto());
    }

    @Test
    @DisplayName("Debe rechazar con SolicitudInvalidaException cuando el tipo de trámite es nulo")
    void debeRechazarCuandoTipoEsNulo() {
        Solicitud solicitud = Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.LEGALIZACION)
                .pais("ES")
                .folios(1)
                .idioma("ES")
                .build();

        assertThrows(SolicitudInvalidaException.class,
                () -> TramiteFactory.crear(null, solicitud));
    }
}

