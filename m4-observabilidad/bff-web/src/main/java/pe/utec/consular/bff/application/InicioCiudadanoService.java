package pe.utec.consular.bff.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import pe.utec.consular.bff.client.CitasClient;
import pe.utec.consular.bff.client.NotificacionesClient;
import pe.utec.consular.bff.client.TramitesClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Servicio de agregación paralela del BFF para la pantalla de inicio del ciudadano ({@code GET /bff/inicio?dni=}).
 * <p>Concepto: Backend For Frontend (BFF – sección 2.2) con consultas concurrentes en hilos virtuales
 * y degradación elegante (*graceful degradation*). Si un servicio tarda más de 2 s o falla,
 * su sección se devuelve vacía ({@code []}) y el motivo se informa en {@code avisosDeDegradacion}.
 */
@Service
public class InicioCiudadanoService {

    private static final Logger log = LoggerFactory.getLogger(InicioCiudadanoService.class);
    private static final long TIMEOUT_SEGUNDOS = 2L;

    private final CitasClient citasClient;
    private final TramitesClient tramitesClient;
    private final NotificacionesClient notificacionesClient;
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public InicioCiudadanoService(CitasClient citasClient,
                                  TramitesClient tramitesClient,
                                  NotificacionesClient notificacionesClient) {
        this.citasClient = citasClient;
        this.tramitesClient = tramitesClient;
        this.notificacionesClient = notificacionesClient;
    }

    public InicioCiudadanoResponse obtenerResumenInicio(String dni) {
        log.info("Agregando en paralelo citas, trámites y avisos para el ciudadano con DNI {}", dni);
        String correlationId = MDC.get("correlationId");
        List<String> avisosDeDegradacion = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<List<Map<String, Object>>> futuroCitas = consultarSeccionAsync(
                "citas-service",
                "El servicio de citas no está disponible temporalmente; sus citas no pudieron cargarse",
                () -> citasClient.listarCitasPorDni(dni),
                correlationId,
                avisosDeDegradacion
        );

        CompletableFuture<List<Map<String, Object>>> futuroTramites = consultarSeccionAsync(
                "tramites-service",
                "El servicio de trámites no está disponible temporalmente; sus trámites no pudieron cargarse",
                () -> tramitesClient.listarTramitesPorDni(dni),
                correlationId,
                avisosDeDegradacion
        );

        CompletableFuture<List<Map<String, Object>>> futuroAvisos = consultarSeccionAsync(
                "notificaciones-service",
                "El servicio de notificaciones no está disponible temporalmente; sus avisos no pudieron cargarse",
                () -> notificacionesClient.listarAvisosPorDni(dni),
                correlationId,
                avisosDeDegradacion
        );

        CompletableFuture.allOf(futuroCitas, futuroTramites, futuroAvisos).join();

        return new InicioCiudadanoResponse(
                dni,
                futuroCitas.join(),
                futuroTramites.join(),
                futuroAvisos.join(),
                List.copyOf(avisosDeDegradacion)
        );
    }

    private CompletableFuture<List<Map<String, Object>>> consultarSeccionAsync(
            String nombreServicio,
            String mensajeDegradacion,
            Supplier<List<Map<String, Object>>> llamada,
            String correlationId,
            List<String> avisosDeDegradacion) {
        return CompletableFuture.supplyAsync(() -> {
                    if (correlationId != null) {
                        MDC.put("correlationId", correlationId);
                    }
                    try {
                        List<Map<String, Object>> resultado = llamada.get();
                        return resultado != null ? resultado : List.<Map<String, Object>>of();
                    } finally {
                        MDC.remove("correlationId");
                    }
                }, virtualExecutor)
                .orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("Degradación elegante en BFF al consultar {}: {}", nombreServicio, ex.getMessage());
                    avisosDeDegradacion.add(mensajeDegradacion);
                    return List.of();
                });
    }

    public record InicioCiudadanoResponse(
            String dni,
            List<Map<String, Object>> citas,
            List<Map<String, Object>> tramites,
            List<Map<String, Object>> avisos,
            List<String> avisosDeDegradacion
    ) {
    }
}

