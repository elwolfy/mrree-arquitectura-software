package pe.utec.consular.bff.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.bff.application.InicioCiudadanoService.InicioCiudadanoResponse;
import pe.utec.consular.bff.client.CitasClient;
import pe.utec.consular.bff.client.NotificacionesClient;
import pe.utec.consular.bff.client.TramitesClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de {@link InicioCiudadanoService} verificando degradación elegante
 * cuando uno de los microservicios consultados falla.
 */
@DisplayName("InicioCiudadanoService – Agregación en paralelo y degradación elegante")
class InicioCiudadanoServiceTest {

    @Test
    @DisplayName("Cuando notificaciones-service está caído, devuelve citas y trámites poblados, avisos vacíos y un aviso de degradación")
    void debeDevolverRespuestaParcialConAvisoCuandoCaeUnServicio() {
        CitasClient citasClient = dni -> List.of(Map.of("codigo", "CIT-2026-000001", "dni", dni));
        TramitesClient tramitesClient = new TramitesClient() {
            @Override
            public List<Map<String, Object>> listarTramitesPorDni(String dni) {
                return List.of(Map.of("codigo", "TRM-2026-000001", "dni", dni));
            }

            @Override
            public Map<String, Object> registrarTramite(Map<String, Object> solicitud) {
                return Map.of();
            }
        };
        NotificacionesClient notificacionesCaido = dni -> {
            throw new RuntimeException("Connection refused: notificaciones-service:8084");
        };

        InicioCiudadanoService service = new InicioCiudadanoService(
                citasClient, tramitesClient, notificacionesCaido);

        InicioCiudadanoResponse respuesta = service.obtenerResumenInicio("40000001");

        assertEquals("40000001", respuesta.dni());
        assertEquals(1, respuesta.citas().size());
        assertEquals(1, respuesta.tramites().size());
        assertTrue(respuesta.avisos().isEmpty(), "La sección de avisos debe devolverse vacía ante falla");
        assertEquals(1, respuesta.avisosDeDegradacion().size(), "Debe incluir exactamente 1 aviso de degradación");
        assertTrue(respuesta.avisosDeDegradacion().getFirst().contains("notificaciones"));
    }
}

