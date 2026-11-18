package pe.utec.consular.notificaciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import pe.utec.consular.notificaciones.adapter.in.messaging.TramiteObservadoEvent;
import pe.utec.consular.notificaciones.adapter.in.web.AvisoDto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integración del consumidor {@code TramiteObservadoConsumer} (Asignación A2 – Tarea 2).
 * <p>En la rama {@code main} está anotada con {@code @Disabled("Asignación A2")} para que el
 * participante cree {@code TramiteObservadoConsumer} y quite la anotación.
 */
@Disabled("Asignación A2")
@Tag("integracion")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("TramiteObservadoConsumer – Consumo idempotente de tramite.observado y envío a DLQ (Asignación A2)")
class TramiteObservadoConsumerTest {

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

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void limpiarColasYCorreos() {
        amqpAdmin.purgeQueue(RabbitMessagingConfig.COLA_TRAMITE_OBSERVADO, false);
        amqpAdmin.purgeQueue(RabbitMessagingConfig.COLA_TRAMITE_OBSERVADO_DLQ, false);
        restTemplate.delete("http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(8025) + "/api/v1/messages");
    }

    @Test
    @DisplayName("Publicar dos veces el mismo tramite.observado (mismo messageId) registra un solo aviso y envía un solo correo")
    void mensajeDuplicadoGeneraUnSoloAviso() throws Exception {
        String messageId = UUID.randomUUID().toString();
        String dni = "40000030";
        TramiteObservadoEvent evento = new TramiteObservadoEvent(
                UUID.randomUUID(),
                "TRM-2026-000030",
                dni,
                "ciudadano30@correo.lab",
                "Falta sello apostillado en el reverso"
        );

        MessagePostProcessor postProcessor = msg -> {
            msg.getMessageProperties().setMessageId(messageId);
            msg.getMessageProperties().setHeader("messageId", messageId);
            msg.getMessageProperties().setHeader("tipoEvento", "tramite.observado");
            msg.getMessageProperties().setHeader("ocurridoEn", Instant.now().toString());
            return msg;
        };

        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.EXCHANGE_EVENTOS,
                RabbitMessagingConfig.RK_TRAMITE_OBSERVADO,
                evento,
                postProcessor
        );
        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.EXCHANGE_EVENTOS,
                RabbitMessagingConfig.RK_TRAMITE_OBSERVADO,
                evento,
                postProcessor
        );

        Thread.sleep(1500);

        ResponseEntity<AvisoDto[]> respAvisos = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/avisos?dni=" + dni, AvisoDto[].class);
        assertEquals(HttpStatus.OK, respAvisos.getStatusCode());
        assertNotNull(respAvisos.getBody());
        assertEquals(1, respAvisos.getBody().length);
        assertTrue(respAvisos.getBody()[0].asunto().contains("Su trámite requiere una corrección"));

        ResponseEntity<Map> respMailpit = restTemplate.getForEntity(
                "http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(8025) + "/api/v1/messages", Map.class);
        assertNotNull(respMailpit.getBody());
        assertEquals(1, respMailpit.getBody().get("total"));
    }

    @Test
    @DisplayName("Un mensaje inválido (sin DNI ni destinatario) se reintenta y termina en notificaciones.tramite.observado.dlq")
    void mensajeInvalidoTerminaEnLaDlq() {
        String messageId = UUID.randomUUID().toString();
        TramiteObservadoEvent eventoInvalido = new TramiteObservadoEvent(
                UUID.randomUUID(),
                "TRM-2026-000031",
                null,
                null,
                "Motivo sin destinatario"
        );

        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.EXCHANGE_EVENTOS,
                RabbitMessagingConfig.RK_TRAMITE_OBSERVADO,
                eventoInvalido,
                msg -> {
                    msg.getMessageProperties().setMessageId(messageId);
                    msg.getMessageProperties().setHeader("messageId", messageId);
                    return msg;
                }
        );

        Message enDlq = rabbitTemplate.receive(RabbitMessagingConfig.COLA_TRAMITE_OBSERVADO_DLQ, 8000);
        assertNotNull(enDlq, "El mensaje inválido debe terminar en notificaciones.tramite.observado.dlq");
        assertEquals(messageId, enDlq.getMessageProperties().getMessageId());
    }
}

