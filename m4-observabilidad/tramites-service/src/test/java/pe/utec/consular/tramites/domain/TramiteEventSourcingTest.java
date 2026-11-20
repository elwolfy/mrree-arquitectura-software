package pe.utec.consular.tramites.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.utec.consular.tramites.domain.evento.EmisionConError;
import pe.utec.consular.tramites.domain.evento.EmisionSolicitada;
import pe.utec.consular.tramites.domain.evento.EventoDominioTramite;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;
import pe.utec.consular.tramites.domain.evento.PagoAnulacionSolicitada;
import pe.utec.consular.tramites.domain.evento.PagoAsociado;
import pe.utec.consular.tramites.domain.evento.TramiteEmitido;
import pe.utec.consular.tramites.domain.evento.TramiteRechazado;
import pe.utec.consular.tramites.domain.evento.TramiteRegistrado;
import pe.utec.consular.tramites.domain.exception.ConflictoVersionException;
import pe.utec.consular.tramites.domain.exception.TransicionEstadoInvalidaException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas unitarias de Event Sourcing y Concurrencia Optimista en el agregado Tramite")
class TramiteEventSourcingTest {

    @Test
    @DisplayName("Reconstruir el agregado desde eventos reproduce el estado final EMITIDO, versión 4 y documento emitido")
    void reconstruyeAgregadoDesdeSecuenciaCompletaDeEventos() {
        UUID tramiteId = UUID.randomUUID();
        UUID pagoId = UUID.randomUUID();
        Instant t1 = Instant.parse("2026-11-19T10:00:00Z");
        Instant t2 = Instant.parse("2026-11-19T10:01:00Z");
        Instant t3 = Instant.parse("2026-11-19T10:02:00Z");
        Instant t4 = Instant.parse("2026-11-19T10:03:00Z");

        List<EventoDominioTramite> historial = List.of(
                new TramiteRegistrado(
                        tramiteId, "TRM-2026-000101", "40000001", "Ana Pérez", "ana@correo.lab",
                        TipoTramite.APOSTILLA, "ES", 2, "ES", new BigDecimal("33.00"), t1
                ),
                new PagoAsociado(tramiteId, pagoId, new BigDecimal("33.00"), t2),
                new EmisionSolicitada(tramiteId, t3),
                new TramiteEmitido(tramiteId, "APO-2026-000101", t4)
        );

        Tramite tramite = Tramite.reconstruirDesdeEventos(historial);

        assertEquals(tramiteId, tramite.getId());
        assertEquals("TRM-2026-000101", tramite.getCodigo().valor());
        assertEquals("Apostilla", tramite.getSubtipo());
        assertEquals(EstadoTramite.EMITIDO, tramite.getEstado());
        assertEquals(4, tramite.getVersion());
        assertEquals(4, tramite.getVersionBase());
        assertEquals(pagoId, tramite.getPagoId());
        assertEquals("APO-2026-000101", tramite.getNumeroDocumento());
        assertEquals(t4, tramite.getActualizadoEn());
        assertTrue(tramite.getEventosPendientes().isEmpty());
    }

    @Test
    @DisplayName("Reconstruir flujo con falla de emisión y rechazo proyecta estado RECHAZADO y motivoRechazo")
    void reconstruyeFlujoDeErrorYRechazo() {
        UUID tramiteId = UUID.randomUUID();
        UUID pagoId = UUID.randomUUID();
        Instant base = Instant.parse("2026-11-19T11:00:00Z");

        List<EventoDominioTramite> historial = List.of(
                new TramiteRegistrado(
                        tramiteId, "TRM-2026-000102", "40000002", "Carlos Mendoza", "carlos@correo.lab",
                        TipoTramite.LEGALIZACION, "ES", 3, "ES", new BigDecimal("22.00"), base
                ),
                new PagoAsociado(tramiteId, pagoId, new BigDecimal("22.00"), base.plusSeconds(10)),
                new EmisionSolicitada(tramiteId, base.plusSeconds(20)),
                new EmisionConError(tramiteId, "Falla en servicio de firma electrónica", base.plusSeconds(30)),
                new PagoAnulacionSolicitada(tramiteId, pagoId, "Compensación por falla de emisión", base.plusSeconds(40)),
                new TramiteRechazado(tramiteId, "Falla en servicio de firma electrónica", base.plusSeconds(50))
        );

        Tramite tramite = Tramite.reconstruirDesdeEventos(historial);

        assertEquals(EstadoTramite.RECHAZADO, tramite.getEstado());
        assertEquals("Falla en servicio de firma electrónica", tramite.getMotivoRechazo());
        assertEquals(6, tramite.getVersion());
        assertEquals("Falla en servicio de firma electrónica", tramite.aVista().motivoRechazo());
    }

    @Test
    @DisplayName("Transición inválida lanza TransicionEstadoInvalidaException")
    void transicionInvalidaLanzaExcepcionDeDominio() {
        Solicitud solicitud = Solicitud.builder()
                .dni("40000001")
                .nombres("Ana Pérez")
                .email("ana@correo.lab")
                .tipo(TipoTramite.APOSTILLA)
                .pais("ES")
                .folios(2)
                .idioma("ES")
                .build();

        Tramite tramite = TramiteFactory.crear(TipoTramite.APOSTILLA, solicitud, new CodigoTramite("TRM-2026-000201"));
        tramite.pasarAPendientePago();
        assertEquals(EstadoTramite.PENDIENTE_PAGO, tramite.getEstado());

        // No se puede solicitar emisión ni marcar emitido si aún está PENDIENTE_PAGO
        assertThrows(TransicionEstadoInvalidaException.class, tramite::solicitarEmision);
        assertThrows(TransicionEstadoInvalidaException.class, () -> tramite.marcarEmitido("DOC-001"));

        // Asociar pago válido -> PAGADO
        tramite.asociarPago(UUID.randomUUID(), tramite.getMonto());
        assertEquals(EstadoTramite.PAGADO, tramite.getEstado());

        // No se puede volver a asociar otro pago cuando ya está PAGADO
        assertThrows(TransicionEstadoInvalidaException.class,
                () -> tramite.asociarPago(UUID.randomUUID(), tramite.getMonto()));
    }

    @Test
    @DisplayName("Conflicto de versión al reconstruir o verificar concurrencia optimista lanza ConflictoVersionException")
    void conflictoDeVersionLanzaConflictoVersionException() {
        UUID tramiteId = UUID.randomUUID();
        TramiteRegistrado ev1 = new TramiteRegistrado(
                tramiteId, "TRM-2026-000301", "40000001", "Ana Pérez", "ana@correo.lab",
                TipoTramite.REGISTRO_CONSULAR, "ES", 1, "ES", new BigDecimal("27.50"), Instant.now()
        );
        PagoAsociado ev2 = new PagoAsociado(
                tramiteId, UUID.randomUUID(), new BigDecimal("27.50"), Instant.now()
        );

        // Secuencia con salto de versión (1 -> 3) debe fallar con ConflictoVersionException
        List<EventoVersionadoTramite> secuenciaConSalto = List.of(
                new EventoVersionadoTramite(1, ev1),
                new EventoVersionadoTramite(3, ev2)
        );
        ConflictoVersionException exSalto = assertThrows(ConflictoVersionException.class,
                () -> Tramite.reconstruirDesdeEventosVersionados(secuenciaConSalto));
        assertNotNull(exSalto.getMessage());

        // Reconstrucción válida en versión 2 y verificación de concurrencia con versión obsoleta (1)
        Tramite tramite = Tramite.reconstruirDesdeEventos(List.of(ev1, ev2));
        assertEquals(2, tramite.getVersionBase());
        assertThrows(ConflictoVersionException.class, () -> tramite.verificarVersionEsperada(1));
    }
}

