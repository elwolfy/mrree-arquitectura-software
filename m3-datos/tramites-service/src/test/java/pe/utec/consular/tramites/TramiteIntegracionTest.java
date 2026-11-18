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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.tramites.adapter.in.grpc.GrpcServerLifecycle;
import pe.utec.consular.tramites.adapter.in.web.RegistrarTramiteRequest;
import pe.utec.consular.tramites.adapter.in.web.TramiteDto;
import pe.utec.consular.tramites.adapter.out.messaging.RabbitConfig;
import pe.utec.consular.tramites.domain.EstadoTramite;
import pe.utec.consular.tramites.domain.TipoTramite;
import pe.utec.consular.tramites.domain.TramiteRegistrado;
import pe.utec.consular.tramites.grpc.v1.ConsultarTramiteRequest;
import pe.utec.consular.tramites.grpc.v1.TramiteResponse;
import pe.utec.consular.tramites.grpc.v1.TramitesServiceGrpc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integracion")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "grpc.port=0"
)
@DisplayName("Pruebas de integración de tramites-service en M3 con Testcontainers (Esquemas aislados, JSONB GIN, RabbitMQ y gRPC)")
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
    @DisplayName("POST /api/tramites con los 3 tipos crea el subtipo correcto en PENDIENTE_PAGO, guarda expediente JSONB, publica tramite.registrado y gRPC coincide con REST")
    void registrarTresTiposPublicaEventoYGrpcCoincideConRest() throws Exception {
        // 1. Registrar LEGALIZACION (metadatos JSONB: folios, idioma)
        TramiteDto legalizacion = registrarViaRest(new RegistrarTramiteRequest(
                "40000001", "Ana Pérez", "ana@correo.lab",
                TipoTramite.LEGALIZACION, "ES", 3, "ES"
        ));
        assertEquals("Legalizacion", legalizacion.subtipo());
        assertEquals(EstadoTramite.PENDIENTE_PAGO, legalizacion.estado());
        verificarEventoRabbit(legalizacion);

        // 2. Registrar APOSTILLA (metadatos JSONB: paisDestino="ES", idioma="ES")
        TramiteDto apostilla = registrarViaRest(new RegistrarTramiteRequest(
                "40000002", "Carlos Mendoza", "carlos@correo.lab",
                TipoTramite.APOSTILLA, "ES", 2, "ES"
        ));
        assertEquals("Apostilla", apostilla.subtipo());
        assertEquals(EstadoTramite.PENDIENTE_PAGO, apostilla.estado());
        verificarEventoRabbit(apostilla);

        // 3. Registrar REGISTRO_CONSULAR (metadatos JSONB: paisDestino="AR", idioma="ES")
        TramiteDto registroConsular = registrarViaRest(new RegistrarTramiteRequest(
                "40000003", "Elena Vargas", "elena@correo.lab",
                TipoTramite.REGISTRO_CONSULAR, "AR", 1, "ES"
        ));
        assertEquals("RegistroConsular", registroConsular.subtipo());
        assertEquals(EstadoTramite.PENDIENTE_PAGO, registroConsular.estado());
        verificarEventoRabbit(registroConsular);

        // 4. Comparar GET /api/tramites/{id} (REST) vs ConsultarTramite (gRPC)
        ResponseEntity<TramiteDto> respRest = restTemplate.getForEntity(
                url("/api/tramites/" + apostilla.id()), TramiteDto.class);
        assertEquals(HttpStatus.OK, respRest.getStatusCode());
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
            assertEquals(dtoRest.nombres(), respGrpc.getNombres());
            assertEquals(dtoRest.email(), respGrpc.getEmail());
            assertEquals(dtoRest.tipoTramite().name(), respGrpc.getTipoTramite());
            assertEquals(dtoRest.subtipo(), respGrpc.getSubtipo());
            assertEquals(dtoRest.pais(), respGrpc.getPais());
            assertEquals(dtoRest.folios(), respGrpc.getFolios());
            assertEquals(dtoRest.idioma(), respGrpc.getIdioma());
            assertEquals(dtoRest.estado().name(), respGrpc.getEstado());
            assertEquals(dtoRest.monto().toPlainString(), respGrpc.getMonto());
        } finally {
            channel.shutdownNow();
        }
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
        assertTrue(idsEncontrados.contains(apostillaEs.id()),
                "Debe incluir la Apostilla cuyo expediente JSONB tiene paisDestino=ES");
        assertTrue(idsEncontrados.contains(registroEs.id()),
                "Debe incluir el Registro Consular cuyo expediente JSONB tiene paisDestino=ES");
        assertFalse(idsEncontrados.contains(apostillaIt.id()),
                "No debe incluir la Apostilla con paisDestino=IT");
        assertFalse(idsEncontrados.contains(legalizacionEs.id()),
                "No debe incluir la Legalización porque su expediente JSONB solo almacena folios e idioma");
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
        assertEquals("42501", ex.getSQLState(),
                "PostgreSQL debe rechazar la lectura cruzada de esquema con código 42501 (insufficient_privilege)");
        assertTrue(ex.getMessage().toLowerCase().contains("tramites"),
                "El mensaje de error debe indicar falta de permisos sobre el esquema tramites: " + ex.getMessage());
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

    private void verificarEventoRabbit(TramiteDto dto) throws Exception {
        Message mensaje = rabbitTemplate.receive(COLA_NOTIFICACIONES_TRAMITE, 5000);
        assertNotNull(mensaje, "Debe recibirse el evento tramite.registrado en la cola");

        Object headerMessageId = mensaje.getMessageProperties().getHeaders().get("messageId");
        Object headerTipoEvento = mensaje.getMessageProperties().getHeaders().get("tipoEvento");
        Object headerOcurridoEn = mensaje.getMessageProperties().getHeaders().get("ocurridoEn");

        assertNotNull(headerMessageId);
        assertDoesNotThrow(() -> UUID.fromString(headerMessageId.toString()));
        assertEquals("tramite.registrado", headerTipoEvento);
        assertNotNull(headerOcurridoEn);

        TramiteRegistrado evento = objectMapper.readValue(mensaje.getBody(), TramiteRegistrado.class);
        assertEquals(dto.id(), evento.tramiteId());
        assertEquals(dto.codigo(), evento.codigo());
        assertEquals(dto.dni(), evento.dni());
        assertEquals(dto.email(), evento.email());
        assertEquals(dto.tipoTramite(), evento.tipoTramite());
    }
}
