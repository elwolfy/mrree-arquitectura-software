package pe.utec.consular.tramites;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.tramites.adapter.in.grpc.GrpcServerLifecycle;
import pe.utec.consular.tramites.adapter.in.web.EventoHistorialDto;
import pe.utec.consular.tramites.adapter.in.web.ReconstruccionProyeccionDto;
import pe.utec.consular.tramites.adapter.in.web.RegistrarTramiteRequest;
import pe.utec.consular.tramites.adapter.in.web.TramiteDto;
import pe.utec.consular.tramites.adapter.out.messaging.RabbitConfig;
import pe.utec.consular.tramites.adapter.out.persistence.OutboxJpaEntity;
import pe.utec.consular.tramites.adapter.out.persistence.SpringDataOutboxRepository;
import pe.utec.consular.tramites.application.port.out.EventoTramiteStorePort;
import pe.utec.consular.tramites.application.saga.SagaSolicitudTramite;
import pe.utec.consular.tramites.domain.EmisionCompletada;
import pe.utec.consular.tramites.domain.EmisionFallida;
import pe.utec.consular.tramites.domain.EstadoTramite;
import pe.utec.consular.tramites.domain.PagoConfirmado;
import pe.utec.consular.tramites.domain.TipoTramite;
import pe.utec.consular.tramites.domain.TramiteRegistrado;
import pe.utec.consular.tramites.domain.evento.EventoVersionadoTramite;
import pe.utec.consular.tramites.domain.evento.PagoAsociado;
import pe.utec.consular.tramites.domain.exception.ConflictoVersionException;
import pe.utec.consular.tramites.grpc.v1.ConsultarTramiteRequest;
import pe.utec.consular.tramites.grpc.v1.TramiteResponse;
import pe.utec.consular.tramites.grpc.v1.TramitesServiceGrpc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integracion")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "grpc.port=0",
                "outbox.relay.intervalo-ms=200"
        }
)
@DisplayName("Pruebas de integración de tramites-service en M3 (Event Sourcing, CQRS, Outbox, JSONB y Esquemas aislados)")
class TramiteIntegracionTest {

    private static final String COLA_NOTIFICACIONES_TRAMITE = "notificaciones.tramite.registrado";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("consular")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("01-esquemas.sql");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "tramites_app");
        registry.add("spring.datasource.password", () -> "tramites_pwd");
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @LocalServerPort
    private int httpPort;

    @Autowired
    private GrpcServerLifecycle grpcServerLifecycle;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EventoTramiteStorePort eventoTramiteStorePort;

    @Autowired
    private SpringDataOutboxRepository outboxRepository;

    @Autowired
    private SagaSolicitudTramite sagaSolicitudTramite;

    private final RestTemplate restTemplate = createRestTemplate();

    private static RestTemplate createRestTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(HttpStatusCode statusCode) {
                return false;
            }
        });
        return rt;
    }

    @BeforeEach
    void prepararTopologiaRabbit() {
        TopicExchange exchange = new TopicExchange(RabbitConfig.EXCHANGE_EVENTOS, true, false);
        Queue queue = new Queue(COLA_NOTIFICACIONES_TRAMITE, true);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(RabbitConfig.ROUTING_KEY_TRAMITE_REGISTRADO));
        amqpAdmin.purgeQueue(COLA_NOTIFICACIONES_TRAMITE, false);
    }

    private String url(String path) {
        return "http://localhost:" + httpPort + path;
    }

    @Test
    @DisplayName("POST /api/tramites escribe en eventos_tramite, proyecta en vista_tramite y publica vía outbox en < 1 s")
    void registrarTresTiposEscribeEventosVistaYOutbox() throws Exception {
        // 1. Registrar LEGALIZACION
        TramiteDto legalizacion = registrarViaRest(new RegistrarTramiteRequest(
                "40000001", "Ana Pérez", "ana@correo.lab",
                TipoTramite.LEGALIZACION, "ES", 3, "ES"
        ));
        assertEquals("Legalizacion", legalizacion.subtipo());
        assertEquals(EstadoTramite.PENDIENTE_PAGO, legalizacion.estado());
        verificarEventoRabbitYOutbox(legalizacion);

        // 2. Registrar APOSTILLA
        TramiteDto apostilla = registrarViaRest(new RegistrarTramiteRequest(
                "40000002", "Carlos Mendoza", "carlos@correo.lab",
                TipoTramite.APOSTILLA, "ES", 2, "ES"
        ));
        assertEquals("Apostilla", apostilla.subtipo());
        assertEquals(EstadoTramite.PENDIENTE_PAGO, apostilla.estado());
        verificarEventoRabbitYOutbox(apostilla);

        // 3. Registrar REGISTRO_CONSULAR
        TramiteDto registroConsular = registrarViaRest(new RegistrarTramiteRequest(
                "40000003", "Elena Vargas", "elena@correo.lab",
                TipoTramite.REGISTRO_CONSULAR, "AR", 1, "ES"
        ));
        assertEquals("RegistroConsular", registroConsular.subtipo());
        assertEquals(EstadoTramite.PENDIENTE_PAGO, registroConsular.estado());
        verificarEventoRabbitYOutbox(registroConsular);

        // 4. Verificar GET /api/tramites/{id}/historial
        ResponseEntity<List<EventoHistorialDto>> respHistorial = restTemplate.exchange(
                url("/api/tramites/" + apostilla.id() + "/historial"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, respHistorial.getStatusCode());
        List<EventoHistorialDto> historial = respHistorial.getBody();
        assertNotNull(historial);
        assertEquals(1, historial.size());
        assertEquals("TramiteRegistrado", historial.get(0).tipo());
        assertEquals(1, historial.get(0).version());
        assertNotNull(historial.get(0).fecha());
        assertEquals(apostilla.codigo(), historial.get(0).datos().get("codigo"));

        // 5. Comparar GET /api/tramites/{id} (REST desde vista_tramite con X-Cache: MISS) vs ConsultarTramite (gRPC desde vista_tramite)
        ResponseEntity<TramiteDto> respRest = restTemplate.getForEntity(
                url("/api/tramites/" + apostilla.id()), TramiteDto.class);
        assertEquals(HttpStatus.OK, respRest.getStatusCode());
        assertEquals("MISS", respRest.getHeaders().getFirst("X-Cache"),
                "En main (CacheNulaConsultaTramiteAdapter), GET /api/tramites/{id} debe responder con X-Cache: MISS");
        TramiteDto dtoRest = respRest.getBody();
        assertNotNull(dtoRest);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", grpcServerLifecycle.getPort())
                .usePlaintext()
                .build();
        try {
            TramitesServiceGrpc.TramitesServiceBlockingStub stub = TramitesServiceGrpc.newBlockingStub(channel);
            TramiteResponse respGrpc = stub.consultarTramite(
                    ConsultarTramiteRequest.newBuilder()
                            .setId(apostilla.id().toString())
                            .build()
            );

            assertEquals(dtoRest.id().toString(), respGrpc.getId());
            assertEquals(dtoRest.codigo(), respGrpc.getCodigo());
            assertEquals(dtoRest.dni(), respGrpc.getDni());
            assertEquals(dtoRest.estado().name(), respGrpc.getEstado());
            assertEquals(dtoRest.monto().toPlainString(), respGrpc.getMonto());
        } finally {
            channel.shutdownNow();
        }
    }

    @Test
    @DisplayName("POST /admin/proyecciones/vista-tramite/reconstruir borra la vista y la reconstruye desde eventos_tramite produciendo el mismo estado")
    void reconstruirProyeccionProduceLaMismaVistaYControlaConcurrenciaOptimista() {
        TramiteDto creado = registrarViaRest(new RegistrarTramiteRequest(
                "40000010", "Sofía Castro", "sofia@correo.lab",
                TipoTramite.APOSTILLA, "ES", 2, "ES"
        ));

        // Verificar concurrencia optimista: intentar guardar versión 2 indicando versionEsperada=0 debe lanzar ConflictoVersionException
        PagoAsociado pagoEvento = new PagoAsociado(creado.id(), UUID.randomUUID(), creado.monto(), Instant.now());
        assertThrows(ConflictoVersionException.class, () ->
                eventoTramiteStorePort.guardarEventos(
                        creado.id(),
                        0,
                        List.of(new EventoVersionadoTramite(2, pagoEvento))
                )
        );

        // Guardar evento PagoAsociado con versionEsperada=1 (correcta) en eventos_tramite sin proyectar aún en vista_tramite
        eventoTramiteStorePort.guardarEventos(
                creado.id(),
                1,
                List.of(new EventoVersionadoTramite(2, pagoEvento))
        );

        // Borrar manualmente la fila de vista_tramite para comprobar que GET /api/tramites/{id} lee SOLO de la vista (devuelve 404)
        jdbcTemplate.update("DELETE FROM tramites.vista_tramite WHERE tramite_id = ?", creado.id());
        ResponseEntity<String> respSinVista = restTemplate.getForEntity(url("/api/tramites/" + creado.id()), String.class);
        assertEquals(HttpStatus.NOT_FOUND, respSinVista.getStatusCode(),
                "Con vista_tramite vacía, GET /api/tramites/{id} debe devolver 404 demostrando que lee solo de la vista");

        // Invocar POST /admin/proyecciones/vista-tramite/reconstruir
        ResponseEntity<ReconstruccionProyeccionDto> respReconstruir = restTemplate.postForEntity(
                url("/admin/proyecciones/vista-tramite/reconstruir"), null, ReconstruccionProyeccionDto.class);
        assertEquals(HttpStatus.OK, respReconstruir.getStatusCode());
        assertNotNull(respReconstruir.getBody());
        assertTrue(respReconstruir.getBody().tramitesReconstruidos() >= 1);

        // Ahora GET /api/tramites/{id} devuelve el trámite reconstruido con estado PAGADO (versión 2)
        ResponseEntity<TramiteDto> respReconstruido = restTemplate.getForEntity(
                url("/api/tramites/" + creado.id()), TramiteDto.class);
        assertEquals(HttpStatus.OK, respReconstruido.getStatusCode());
        assertNotNull(respReconstruido.getBody());
        assertEquals(creado.codigo(), respReconstruido.getBody().codigo());
        assertEquals(EstadoTramite.PAGADO, respReconstruido.getBody().estado());

        // Y el historial muestra las 2 versiones en orden
        ResponseEntity<List<EventoHistorialDto>> respHistorial = restTemplate.exchange(
                url("/api/tramites/" + creado.id() + "/historial"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, respHistorial.getStatusCode());
        assertNotNull(respHistorial.getBody());
        assertEquals(2, respHistorial.getBody().size());
        assertEquals("TramiteRegistrado", respHistorial.getBody().get(0).tipo());
        assertEquals(1, respHistorial.getBody().get(0).version());
        assertEquals("PagoAsociado", respHistorial.getBody().get(1).tipo());
        assertEquals(2, respHistorial.getBody().get(1).version());
    }

    @Test
    @DisplayName("Outbox transaccional: con RabbitMQ pausado el evento queda en outbox (publicado_en IS NULL) y se publica al recuperarse")
    void outboxGuardaPendienteCuandoRabbitMqEstaDetenidoYPublicaAlRecuperarse() throws Exception {
        // Vaciar cualquier mensaje previo de la cola
        amqpAdmin.purgeQueue(COLA_NOTIFICACIONES_TRAMITE, false);

        // Pausar contenedor RabbitMQ para simular caída del broker durante el registro del trámite
        rabbitmq.getDockerClient().pauseContainerCmd(rabbitmq.getContainerId()).exec();
        TramiteDto tramiteRegistradoSinBroker;
        try {
            tramiteRegistradoSinBroker = registrarViaRest(new RegistrarTramiteRequest(
                    "40000020", "Martín Vizcarra", "martin@correo.lab",
                    TipoTramite.LEGALIZACION, "ES", 2, "ES"
            ));
            assertEquals(EstadoTramite.PENDIENTE_PAGO, tramiteRegistradoSinBroker.estado());

            // Verificar que el registro existe en tramites.outbox con publicado_en IS NULL
            OutboxJpaEntity registroPendiente = outboxRepository.findAll().stream()
                    .filter(o -> o.getAgregadoId().equals(tramiteRegistradoSinBroker.id()))
                    .findFirst()
                    .orElseThrow();
            assertNull(registroPendiente.getPublicadoEn(),
                    "Mientras RabbitMQ está detenido, publicado_en debe permanecer NULL en tramites.outbox");
        } finally {
            // Reanudar RabbitMQ
            rabbitmq.getDockerClient().unpauseContainerCmd(rabbitmq.getContainerId()).exec();
        }

        // Al recuperarse RabbitMQ, RelayOutbox publica el mensaje pendiente en menos de 1-2 s y marca publicado_en
        verificarEventoRabbitYOutbox(tramiteRegistradoSinBroker);
    }

    @Test
    @DisplayName("GET /api/tramites/buscar?pais=ES filtra trámites mediante el operador JSONB @> sobre tramites.expediente.metadatos")
    void buscarPorPaisFiltraConOperadorJsonbContencion() {
        TramiteDto apostillaEs = registrarViaRest(new RegistrarTramiteRequest(
                "41000001", "Lucía Gómez", "lucia@correo.lab",
                TipoTramite.APOSTILLA, "ES", 4, "ES"
        ));
        TramiteDto registroEs = registrarViaRest(new RegistrarTramiteRequest(
                "41000002", "Jorge Ríos", "jorge@correo.lab",
                TipoTramite.REGISTRO_CONSULAR, "ES", 1, "ES"
        ));
        TramiteDto apostillaIt = registrarViaRest(new RegistrarTramiteRequest(
                "41000003", "Mario Rossi", "mario@correo.lab",
                TipoTramite.APOSTILLA, "IT", 2, "IT"
        ));
        TramiteDto legalizacionEs = registrarViaRest(new RegistrarTramiteRequest(
                "41000004", "Pedro Salas", "pedro@correo.lab",
                TipoTramite.LEGALIZACION, "ES", 5, "ES"
        ));

        ResponseEntity<List<TramiteDto>> response = restTemplate.exchange(
                url("/api/tramites/buscar?pais=ES"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<TramiteDto> encontrados = response.getBody();
        assertNotNull(encontrados);

        List<UUID> idsEncontrados = encontrados.stream().map(TramiteDto::id).toList();
        assertTrue(idsEncontrados.contains(apostillaEs.id()));
        assertTrue(idsEncontrados.contains(registroEs.id()));
        assertFalse(idsEncontrados.contains(apostillaIt.id()));
        assertFalse(idsEncontrados.contains(legalizacionEs.id()));
    }

    @Test
    @DisplayName("Aislamiento Database-per-Service: el usuario pagos_app NO puede leer tramites.expediente")
    void usuarioPagosAppNoPuedeLeerEsquemaTramites() {
        SQLException ex = assertThrows(SQLException.class, () -> {
            try (Connection conn = DriverManager.getConnection(
                    postgres.getJdbcUrl(), "pagos_app", "pagos_pwd");
                 Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM tramites.expediente");
            }
        });
        assertEquals("42501", ex.getSQLState());
        assertTrue(ex.getMessage().toLowerCase().contains("tramites"));
    }

    @Test
    @DisplayName("Saga Orquestada: camino feliz avanza hasta EMITIDO y ante emision.fallida en main queda EN_EMISION sin compensar")
    void sagaCaminoFelizHastaEmitidoYEnMainSinCompensarAnteFalla() {
        // 1. Camino feliz: PENDIENTE_PAGO -> pago.confirmado (EN_EMISION) -> emision.completada (EMITIDO)
        TramiteDto feliz = registrarViaRest(new RegistrarTramiteRequest(
                "42000001", "Laura Paredes", "laura@correo.lab",
                TipoTramite.APOSTILLA, "ES", 2, "ES"
        ));
        assertEquals(EstadoTramite.PENDIENTE_PAGO, feliz.estado());

        UUID pagoIdFeliz = UUID.randomUUID();
        sagaSolicitudTramite.alConfirmarPago(new PagoConfirmado(pagoIdFeliz, feliz.id(), feliz.monto()));
        // Reentrega idempotente del mismo evento
        sagaSolicitudTramite.alConfirmarPago(new PagoConfirmado(pagoIdFeliz, feliz.id(), feliz.monto()));

        ResponseEntity<TramiteDto> enEmision = restTemplate.getForEntity(url("/api/tramites/" + feliz.id()), TramiteDto.class);
        assertEquals(HttpStatus.OK, enEmision.getStatusCode());
        assertNotNull(enEmision.getBody());
        assertEquals(EstadoTramite.EN_EMISION, enEmision.getBody().estado());

        sagaSolicitudTramite.alCompletarEmision(new EmisionCompletada(feliz.id(), "DOC-2026-000099"));
        ResponseEntity<TramiteDto> emitido = restTemplate.getForEntity(url("/api/tramites/" + feliz.id()), TramiteDto.class);
        assertEquals(HttpStatus.OK, emitido.getStatusCode());
        assertNotNull(emitido.getBody());
        assertEquals(EstadoTramite.EMITIDO, emitido.getBody().estado());
        assertEquals("DOC-2026-000099", emitido.getBody().numeroDocumento());

        // 2. Falla de emisión en main: el trámite queda en EN_EMISION (la compensación es la Asignación A3)
        TramiteDto conFalla = registrarViaRest(new RegistrarTramiteRequest(
                "42000002", "Roberto Silva", "roberto@correo.lab",
                TipoTramite.LEGALIZACION, "ES", 3, "ES"
        ));
        sagaSolicitudTramite.alConfirmarPago(new PagoConfirmado(UUID.randomUUID(), conFalla.id(), conFalla.monto()));
        sagaSolicitudTramite.alFallarEmision(new EmisionFallida(conFalla.id(), "Falla simulada de firma"));

        ResponseEntity<TramiteDto> trasFalla = restTemplate.getForEntity(url("/api/tramites/" + conFalla.id()), TramiteDto.class);
        assertEquals(HttpStatus.OK, trasFalla.getStatusCode());
        assertNotNull(trasFalla.getBody());
        assertEquals(EstadoTramite.EN_EMISION, trasFalla.getBody().estado(),
                "En main, ante emision.fallida el trámite debe permanecer en EN_EMISION sin compensar");
    }

    private TramiteDto registrarViaRest(RegistrarTramiteRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<TramiteDto> response = restTemplate.postForEntity(
                url("/api/tramites"), new HttpEntity<>(request, headers), TramiteDto.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private void verificarEventoRabbitYOutbox(TramiteDto dto) throws Exception {
        Message mensaje = null;
        long inicio = System.currentTimeMillis();
        while (System.currentTimeMillis() - inicio < 5000) {
            Message recibido = rabbitTemplate.receive(COLA_NOTIFICACIONES_TRAMITE, 500);
            if (recibido != null) {
                TramiteRegistrado ev = objectMapper.readValue(recibido.getBody(), TramiteRegistrado.class);
                if (dto.id().equals(ev.tramiteId())) {
                    mensaje = recibido;
                    break;
                }
            }
        }
        assertNotNull(mensaje, "Debe recibirse el evento tramite.registrado publicado por RelayOutbox");

        Object headerMessageId = mensaje.getMessageProperties().getHeaders().get("messageId");
        Object headerTipoEvento = mensaje.getMessageProperties().getHeaders().get("tipoEvento");
        Object headerOcurridoEn = mensaje.getMessageProperties().getHeaders().get("ocurridoEn");

        assertNotNull(headerMessageId);
        assertDoesNotThrow(() -> UUID.fromString(headerMessageId.toString()));
        assertEquals("tramite.registrado", headerTipoEvento);
        assertNotNull(headerOcurridoEn);

        OutboxJpaEntity registroOutbox = outboxRepository.findAll().stream()
                .filter(o -> o.getAgregadoId().equals(dto.id()))
                .findFirst()
                .orElseThrow();
        assertNotNull(registroOutbox.getPublicadoEn(),
                "El registro en tramites.outbox debe tener publicado_en marcado tras publicarse");
    }
}
