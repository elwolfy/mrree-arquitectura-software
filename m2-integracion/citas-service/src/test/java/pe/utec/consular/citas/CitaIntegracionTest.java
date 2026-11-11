package pe.utec.consular.citas;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pe.utec.consular.citas.adapter.in.web.CitaDto;
import pe.utec.consular.citas.adapter.in.web.DisponibilidadDto;
import pe.utec.consular.citas.adapter.in.web.OficinaDto;
import pe.utec.consular.citas.adapter.in.web.RegistrarCitaRequest;
import pe.utec.consular.citas.adapter.out.messaging.RabbitConfig;
import pe.utec.consular.citas.domain.CitaReservada;
import pe.utec.consular.citas.domain.TipoTramite;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integracion")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas de integración de citas-service con Testcontainers (PostgreSQL + RabbitMQ)")
class CitaIntegracionTest {

    private static final String COLA_NOTIFICACIONES_CITA = "notificaciones.cita.reservada";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("consular")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @LocalServerPort
    private int port;

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
        Queue queue = new Queue(COLA_NOTIFICACIONES_CITA, true);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(RabbitConfig.ROUTING_KEY_CITA_RESERVADA));
        amqpAdmin.purgeQueue(COLA_NOTIFICACIONES_CITA, false);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("Reservar una cita publica exactamente un mensaje cita.reservada con messageId y cabeceras del contrato")
    void reservarCitaPublicaEventoEnRabbitMQ() throws Exception {
        LocalDate fechaCita = LocalDate.now().plusDays(10);
        LocalTime horaCita = LocalTime.of(8, 0);

        // 1. Verificar catálogo de oficinas y disponibilidad
        ResponseEntity<OficinaDto[]> respOficinas = restTemplate.getForEntity(url("/api/oficinas"), OficinaDto[].class);
        assertEquals(HttpStatus.OK, respOficinas.getStatusCode());
        assertNotNull(respOficinas.getBody());
        assertTrue(respOficinas.getBody().length >= 6);

        ResponseEntity<DisponibilidadDto> respDisp = restTemplate.getForEntity(
                url("/api/oficinas/MAD/disponibilidad?fecha=" + fechaCita), DisponibilidadDto.class);
        assertEquals(HttpStatus.OK, respDisp.getStatusCode());

        // 2. Reservar cita
        RegistrarCitaRequest peticion = new RegistrarCitaRequest(
                "40000001",
                "Ana Pérez",
                "ana@correo.lab",
                "MAD",
                fechaCita,
                horaCita,
                TipoTramite.LEGALIZACION
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<CitaDto> respReserva = restTemplate.postForEntity(
                url("/api/citas"), new HttpEntity<>(peticion, headers), CitaDto.class);

        assertEquals(HttpStatus.CREATED, respReserva.getStatusCode());
        CitaDto citaCreada = respReserva.getBody();
        assertNotNull(citaCreada);
        assertEquals("40000001", citaCreada.dni());
        assertEquals("MAD", citaCreada.oficinaId());
        assertEquals("PENDIENTE", citaCreada.notificacion());

        // 3. Verificar que se publicó exactamente 1 mensaje en la cola notificaciones.cita.reservada
        Message mensaje = rabbitTemplate.receive(COLA_NOTIFICACIONES_CITA, 5000);
        assertNotNull(mensaje, "El evento cita.reservada debe recibirse en la cola de notificaciones");

        String messageIdProp = mensaje.getMessageProperties().getMessageId();
        Object headerMessageId = mensaje.getMessageProperties().getHeaders().get("messageId");
        Object headerTipoEvento = mensaje.getMessageProperties().getHeaders().get("tipoEvento");
        Object headerOcurridoEn = mensaje.getMessageProperties().getHeaders().get("ocurridoEn");

        assertNotNull(messageIdProp, "La propiedad messageId debe estar presente");
        assertNotNull(headerMessageId, "El header messageId debe estar presente");
        assertDoesNotThrow(() -> UUID.fromString(headerMessageId.toString()));
        assertEquals("cita.reservada", headerTipoEvento);
        assertNotNull(headerOcurridoEn, "El header ocurridoEn debe estar presente");

        CitaReservada evento = objectMapper.readValue(mensaje.getBody(), CitaReservada.class);
        assertEquals(citaCreada.id(), evento.citaId());
        assertEquals(citaCreada.codigo(), evento.codigo());
        assertEquals("40000001", evento.dni());
        assertEquals("ana@correo.lab", evento.email());
        assertEquals("MAD", evento.oficinaId());
        assertEquals(fechaCita, evento.fecha());
        assertEquals(horaCita, evento.hora());

        // Exactamente un mensaje: una segunda lectura debe devolver null
        Message segundoMensaje = rabbitTemplate.receive(COLA_NOTIFICACIONES_CITA, 500);
        assertNull(segundoMensaje, "Solo debe haberse publicado un único mensaje por reserva");

        // 4. Verificar rechazo por cita duplicada (409 cita-duplicada) y que NO publica mensaje adicional
        ResponseEntity<Map> respDuplicada = restTemplate.postForEntity(
                url("/api/citas"), new HttpEntity<>(peticion, headers), Map.class);
        assertEquals(HttpStatus.CONFLICT, respDuplicada.getStatusCode());
        assertNotNull(respDuplicada.getBody());
        assertEquals("cita-duplicada", respDuplicada.getBody().get("codigo"));
        assertNull(rabbitTemplate.receive(COLA_NOTIFICACIONES_CITA, 300));

        // 5. Cancelar cita (204)
        ResponseEntity<Void> respCancel = restTemplate.exchange(
                url("/api/citas/" + citaCreada.id()), HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, respCancel.getStatusCode());
    }

    @Test
    @DisplayName("Contrato OpenAPI expone los endpoints requeridos de citas y oficinas")
    void verificaContratoOpenApi() {
        ResponseEntity<String> respDocs = restTemplate.getForEntity(url("/v3/api-docs"), String.class);
        assertEquals(HttpStatus.OK, respDocs.getStatusCode());
        assertNotNull(respDocs.getBody());
        assertTrue(respDocs.getBody().contains("\"/api/citas\""));
        assertTrue(respDocs.getBody().contains("\"/api/oficinas\""));
    }
}

