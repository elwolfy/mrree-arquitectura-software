package pe.utec.consular.tramites.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.tramites.domain.exception.SolicitudInvalidaException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de {@link Solicitud} y su {@link Solicitud.Builder} (Patrón Builder).
 */
@DisplayName("Solicitud.Builder – Validaciones obligatorias, coherencia por tipo e inmutabilidad")
class SolicitudBuilderTest {

    @Test
    @DisplayName("Debe rechazar cuando faltan datos obligatorios (DNI, tipo, país, folios o idioma)")
    void debeRechazarCuandoFaltanDatosObligatorios() {
        // Sin DNI
        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .tipo(TipoTramite.LEGALIZACION)
                .pais("ES")
                .folios(1)
                .idioma("ES")
                .build());

        // Sin TipoTramite
        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .pais("ES")
                .folios(1)
                .idioma("ES")
                .build());

        // Sin país válido
        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.LEGALIZACION)
                .folios(1)
                .idioma("ES")
                .build());

        // Sin folios positivos
        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.LEGALIZACION)
                .pais("ES")
                .folios(0)
                .idioma("ES")
                .build());

        // Sin idioma
        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.LEGALIZACION)
                .pais("ES")
                .folios(1)
                .build());
    }

    @Test
    @DisplayName("Debe validar la coherencia cruzada por cada TipoTramite en build()")
    void debeValidarCoherenciaPorTipo() {
        // LEGALIZACION con más de 50 folios -> inválido
        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.LEGALIZACION)
                .pais("ES")
                .folios(51)
                .idioma("ES")
                .build());

        // APOSTILLA con más de 20 folios o país no adherido a La Haya (CU) -> inválido
        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.APOSTILLA)
                .pais("ES")
                .folios(25)
                .idioma("ES")
                .build());

        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.APOSTILLA)
                .pais("CU")
                .folios(1)
                .idioma("ES")
                .build());

        // REGISTRO_CONSULAR con más de 1 folio o idioma distinto de ES -> inválido
        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.REGISTRO_CONSULAR)
                .pais("ES")
                .folios(2)
                .idioma("ES")
                .build());

        assertThrows(SolicitudInvalidaException.class, () -> Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.REGISTRO_CONSULAR)
                .pais("ES")
                .folios(1)
                .idioma("EN")
                .build());
    }

    @Test
    @DisplayName("La instancia de Solicitud construida debe ser inmutable")
    void debeConstruirInstanciaInmutable() {
        Map<String, String> mapaOriginal = new HashMap<>();
        mapaOriginal.put("apostillaElectronica", "SI");

        Solicitud solicitud = Solicitud.builder()
                .dni("40000001")
                .tipo(TipoTramite.APOSTILLA)
                .pais("es")
                .folios(2)
                .idioma("es")
                .metadatos(mapaOriginal)
                .build();

        // Modificar el mapa externo no altera la solicitud
        mapaOriginal.put("apostillaElectronica", "NO");
        assertEquals("SI", solicitud.getMetadatos().get("apostillaElectronica"));
        assertEquals("ES", solicitud.getPais());
        assertEquals("ES", solicitud.getIdioma());

        // El mapa expuesto es inmodificable
        assertThrows(UnsupportedOperationException.class,
                () -> solicitud.getMetadatos().put("otro", "valor"));

        // Todos los campos de instancia en Solicitud son final
        for (Field field : Solicitud.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                assertTrue(Modifier.isFinal(field.getModifiers()),
                        "El campo '" + field.getName() + "' de Solicitud debe ser final");
            }
        }
    }
}

