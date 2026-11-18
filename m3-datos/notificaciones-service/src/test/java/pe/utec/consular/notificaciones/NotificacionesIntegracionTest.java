package pe.utec.consular.notificaciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.notificaciones.adapter.in.messaging.RabbitMessagingConfig;
import pe.utec.consular.notificaciones.adapter.in.messaging.TramiteRegistradoEvent;
import pe.utec.consular.notificaciones.adapter.in.web.AvisoDto;
import pe.utec.consular.notificaciones.application.service.ProcesarNotificacionService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integracion")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas de integración de notificaciones-service (Idempotencia por messageId, Mailpit y DLQ)")
class NotificacionesIntegracionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("consular")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management");

    @Container
    static GenericContainer<?> mailpit = new GenericContainer<>("axllent/mailpit:v1.21")
            .withExposedPorts(1025, 8025);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private ProcesarNotificacionService procesarNotificacionService;

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void prepararEntorno() {
        procesarNotificacionService.setFallarSiempre(false);
        amqpAdmin.purgeQueue(RabbitMessagingConfig.COLA_TRAMITE_REGISTRADO, false);
        amqpAdmin.purgeQueue(RabbitMessagingConfig.COLA_TRAMITE_REGISTRADO_DLQ, false);
        restTemplate.delete("http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(8025) + "/api/v1/messages");
    }

    @Test
    @DisplayName("Publicar dos veces el mismo tramite.registrado con igual messageId genera un solo aviso y un solo correo")
    void idempotenciaPorMessageIdGeneraUnSoloAvisoYUnSoloCorreo() throws Exception {
        String messageId = UUID.randomUUID().toString();
        String dni = "40000025";
        TramiteRegistradoEvent evento = new TramiteRegistradoEvent(
                UUID.randomUUID(),
                "TRM-2026-000025",
                dni,
                "ciudadano25@correo.lab",
                "APOSTILLA"
        );

        MessagePostProcessor postProcessor = msg -> {
            msg.getMessageProperties().setMessageId(messageId);
            msg.getMessageProperties().setHeader("messageId", messageId);
            msg.getMessageProperties().setHeader("tipoEvento", "tramite.registrado");
            msg.getMessageProperties().setHeader("ocurridoEn", Instant.now().toString());
            return msg;
        };

        // Publicar dos veces el mismo mensaje con el mismo messageId
        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.EXCHANGE_EVENTOS,
                RabbitMessagingConfig.RK_TRAMITE_REGISTRADO,
                evento,
                postProcessor
        );
        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.EXCHANGE_EVENTOS,
                RabbitMessagingConfig.RK_TRAMITE_REGISTRADO,
                evento,
                postProcessor
        );

        // Esperar procesamiento asíncrono
        Thread.sleep(1500);

        // 1. Verificar un único aviso en la base de datos por GET /api/avisos?dni=
        ResponseEntity<AvisoDto[]> respAvisos = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/avisos?dni=" + dni, AvisoDto[].class);
        assertEquals(HttpStatus.OK, respAvisos.getStatusCode());
        assertNotNull(respAvisos.getBody());
        assertEquals(1, respAvisos.getBody().length, "Solo debe haberse registrado 1 aviso por idempotencia");

        // 2. Verificar un único correo en Mailpit
        ResponseEntity<Map> respMailpit = restTemplate.getForEntity(
                "http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(8025) + "/api/v1/messages", Map.class);
        assertEquals(HttpStatus.OK, respMailpit.getStatusCode());
        assertNotNull(respMailpit.getBody());
        assertEquals(1, respMailpit.getBody().get("total"), "Solo debe haberse enviado 1 correo a Mailpit");
    }

    @Test
    @DisplayName("Con NOTIFICACIONES_FALLAR_SIEMPRE=true, tras agotar los reintentos el mensaje termina en notificaciones.tramite.registrado.dlq")
    void cuandoFallarSiempreEstaActivoElMensajeTerminaEnLaDlq() {
        procesarNotificacionService.setFallarSiempre(true);

        String messageId = UUID.randomUUID().toString();
        TramiteRegistradoEvent evento = new TramiteRegistradoEvent(
                UUID.randomUUID(),
                "TRM-2026-000099",
                "40000026",
                "falla@correo.lab",
                "LEGALIZACION"
        );

        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.EXCHANGE_EVENTOS,
                RabbitMessagingConfig.RK_TRAMITE_REGISTRADO,
                evento,
                msg -> {
                    msg.getMessageProperties().setMessageId(messageId);
                    msg.getMessageProperties().setHeader("messageId", messageId);
                    return msg;
                }
        );

        Message mensajeDlq = rabbitTemplate.receive(RabbitMessagingConfig.COLA_TRAMITE_REGISTRADO_DLQ, 8000);
        assertNotNull(mensajeDlq, "El mensaje fallido debe haber sido encaminado a la DLQ tras los reintentos");
        assertEquals(messageId, mensajeDlq.getMessageProperties().getMessageId());
    }
}

