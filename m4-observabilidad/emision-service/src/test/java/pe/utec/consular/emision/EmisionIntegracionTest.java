package pe.utec.consular.emision;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.utec.consular.emision.adapter.in.messaging.EmisionSolicitarCommand;
import pe.utec.consular.emision.adapter.in.messaging.RabbitEmisionConfig;
import pe.utec.consular.emision.application.service.ProcesarSolicitudEmisionService;
import pe.utec.consular.emision.domain.EmisionCompletada;
import pe.utec.consular.emision.domain.EmisionFallida;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integracion")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "emision.duracion-ms=50"
)
@DisplayName("Pruebas de integración de emision-service en M3 (Camino feliz, Idempotencia y Falla simulada)")
class EmisionIntegracionTest {

    private static final String COLA_COMPLETADA = "test.emision.completada";
    private static final String COLA_FALLIDA = "test.emision.fallida";

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
        registry.add("spring.datasource.username", () -> "emision_app");
        registry.add("spring.datasource.password", () -> "emision_pwd");
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcesarSolicitudEmisionService emisionService;

    @BeforeEach
    void prepararRabbit() {
        emisionService.setSimularFalla(false);
        TopicExchange exchange = new TopicExchange(RabbitEmisionConfig.EXCHANGE_EVENTOS, true, false);
        Queue qCompletada = new Queue(COLA_COMPLETADA, true);
        Queue qFallida = new Queue(COLA_FALLIDA, true);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(qCompletada);
        amqpAdmin.declareQueue(qFallida);
        amqpAdmin.declareBinding(BindingBuilder.bind(qCompletada).to(exchange).with(RabbitEmisionConfig.RK_EMISION_COMPLETADA));
        amqpAdmin.declareBinding(BindingBuilder.bind(qFallida).to(exchange).with(RabbitEmisionConfig.RK_EMISION_FALLIDA));
        amqpAdmin.purgeQueue(COLA_COMPLETADA, false);
        amqpAdmin.purgeQueue(COLA_FALLIDA, false);
    }

    @Test
    @DisplayName("Camino feliz e idempotencia: consume emision.solicitar y publica un único emision.completada con DOC-YYYY-NNNNNN")
    void caminoFelizPublicaEmisionCompletadaYEsIdempotente() throws Exception {
        UUID tramiteId = UUID.randomUUID();
        String messageId = UUID.randomUUID().toString();
        EmisionSolicitarCommand comando = new EmisionSolicitarCommand(tramiteId, "TRM-2026-000001", "APOSTILLA");

        MessagePostProcessor pp = msg -> {
            msg.getMessageProperties().setMessageId(messageId);
            msg.getMessageProperties().setHeader("messageId", messageId);
            msg.getMessageProperties().setHeader("tipoEvento", RabbitEmisionConfig.RK_EMISION_SOLICITAR);
            msg.getMessageProperties().setHeader("ocurridoEn", Instant.now().toString());
            return msg;
        };

        // Enviar comando por primera vez y un duplicado con el mismo messageId
        rabbitTemplate.convertAndSend(RabbitEmisionConfig.EXCHANGE_EVENTOS, RabbitEmisionConfig.RK_EMISION_SOLICITAR, comando, pp);
        rabbitTemplate.convertAndSend(RabbitEmisionConfig.EXCHANGE_EVENTOS, RabbitEmisionConfig.RK_EMISION_SOLICITAR, comando, pp);

        Message recibido = rabbitTemplate.receive(COLA_COMPLETADA, 5000);
        assertNotNull(recibido, "Debe recibirse el evento emision.completada");
        EmisionCompletada evento = objectMapper.readValue(recibido.getBody(), EmisionCompletada.class);
        assertEquals(tramiteId, evento.tramiteId());
        assertNotNull(evento.numeroDocumento());
        assertTrue(evento.numeroDocumento().matches("^DOC-\\d{4}-\\d{6}$"),
                "El número de documento debe seguir el formato DOC-<año>-<6 dígitos>: " + evento.numeroDocumento());

        // Verificar que el duplicado fue ignorado gracias a emision.mensaje_procesado
        Message segundo = rabbitTemplate.receive(COLA_COMPLETADA, 500);
        assertNull(segundo, "El comando duplicado no debe publicar un segundo evento emision.completada");
    }

    @Test
    @DisplayName("Falla simulada: con emision.simular-falla=true publica emision.fallida con el motivo")
    void fallaSimuladaPublicaEmisionFallida() throws Exception {
        emisionService.setSimularFalla(true);
        UUID tramiteId = UUID.randomUUID();
        String messageId = UUID.randomUUID().toString();
        EmisionSolicitarCommand comando = new EmisionSolicitarCommand(tramiteId, "TRM-2026-000002", "LEGALIZACION");

        MessagePostProcessor pp = msg -> {
            msg.getMessageProperties().setMessageId(messageId);
            msg.getMessageProperties().setHeader("messageId", messageId);
            msg.getMessageProperties().setHeader("tipoEvento", RabbitEmisionConfig.RK_EMISION_SOLICITAR);
            msg.getMessageProperties().setHeader("ocurridoEn", Instant.now().toString());
            return msg;
        };

        rabbitTemplate.convertAndSend(RabbitEmisionConfig.EXCHANGE_EVENTOS, RabbitEmisionConfig.RK_EMISION_SOLICITAR, comando, pp);

        Message recibido = rabbitTemplate.receive(COLA_FALLIDA, 5000);
        assertNotNull(recibido, "Debe recibirse el evento emision.fallida");
        EmisionFallida evento = objectMapper.readValue(recibido.getBody(), EmisionFallida.class);
        assertEquals(tramiteId, evento.tramiteId());
        assertNotNull(evento.motivo());
        assertTrue(evento.motivo().contains("EMISION_SIMULAR_FALLA"));
    }
}

