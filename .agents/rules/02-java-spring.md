---
description: Convenciones Java 21 y Spring Boot para todos los servicios backend
activation: glob
glob: "**/*.java,**/pom.xml,**/application*.yml"
---

# Java y Spring Boot

## Versiones

- Java 21. Usar `record` para DTO, comandos, eventos y objetos de valor inmutables; `sealed` cuando aporte claridad.
- Spring Boot y Spring Cloud según el contrato (sección 2). Un solo valor de versión por POM agregador (`<properties>`), nunca versiones sueltas por dependencia cuando el BOM las gestiona.
- Dependencias permitidas (no agregar otras sin justificar en el Implementation Plan): spring-boot-starter-webmvc/web, validation, data-jpa, actuator, amqp, data-redis, cache; springdoc-openapi; flyway-core + flyway-database-postgresql; postgresql; caffeine; resilience4j (spring-cloud-starter-circuitbreaker-resilience4j); spring-cloud-starter-gateway-server-webmvc o webflux (solo gateway); spring-cloud-function-web (solo serverless); grpc-spring (net.devh o spring-grpc, el que sea compatible con la versión de Boot); spring-modulith; archunit-junit5; testcontainers (postgresql, rabbitmq); micrometer-registry-otlp; logstash-logback-encoder; lombok **no** se usa (código explícito para fines didácticos).

## Estructura de cada servicio

```text
<servicio>/
├── Dockerfile
├── pom.xml
└── src/
    ├── main/java/pe/utec/consular/<contexto>/...
    ├── main/resources/application.yml
    ├── main/resources/db/migration/<esquema>/V1__esquema.sql
    └── test/java/... (unitarias, ArchUnit, integración con Testcontainers)
```

## Configuración

- `application.yml` con valores por defecto para Docker (hosts `postgres`, `rabbitmq`, `redis`, `mailpit`, `lgtm`).
- Perfil `local` (`application-local.yml`) para ejecutar desde Eclipse en **modo híbrido**: hosts `localhost` y puertos publicados del contrato. Documentar en el README del servicio: *Run As → Spring Boot App* con perfil activo `local`.
- Toda propiedad propia del curso bajo prefijos documentados: `consistencia.*`, `notificaciones.*`, `emision.*`, `latencia.*`, mapeadas con `@ConfigurationProperties` + `record`.
- HikariCP: `connection-timeout: 2000`, `validation-timeout: 1000` para que la simulación de partición responda rápido.
- `server.shutdown: graceful`. Actuator: exponer `health`, `info`, `metrics`, `prometheus` (M4).
- `spring.jpa.open-in-view: false`; `ddl-auto: validate` (el esquema lo crea Flyway).

## API REST

- Controladores en `adapter.in.web`, sufijo `Controller`. DTO como `record` con sufijo `Request`/`Dto`.
- Validación con Jakarta Validation; mensajes en español.
- Manejo de errores centralizado con `@RestControllerAdvice` que devuelve `ProblemDetail` (RFC 9457): `type`, `title`, `status`, `detail`, `instance`, y propiedad `codigo`.
- Códigos de error (`codigo`): `validacion`, `no-encontrado`, `cita-duplicada`, `cupo-sin-capacidad`, `base-de-datos-no-disponible`, `identidad-no-valida`, `pago-duplicado`, `conflicto-estado`.
- OpenAPI: título, descripción, versión y etiquetas en español; ejemplos en los DTO con `@Schema(example=…)`.
- CORS: permitir `http://localhost:4200` en el punto de entrada (monolito/servicio/gateway).

## Persistencia

- Repositorios Spring Data solo en `adapter.out.persistence`.
- Transacciones (`@Transactional`) en servicios de aplicación, nunca en controladores.
- Bloqueo pesimista para cupos: `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
- Índices declarados en migraciones, con comentario SQL que explique por qué existen.

## Mensajería (M2+)

- Publicación vía puerto de salida `PublicadorDeEventosPort`; implementación RabbitMQ en `adapter.out.messaging`.
- Consumidores idempotentes: tabla `mensaje_procesado(message_id pk, procesado_en)` por servicio consumidor.
- Reintentos: 3 con backoff exponencial; luego DLQ.
- Serialización JSON con `Jackson2JsonMessageConverter` (o el equivalente de la versión de Spring AMQP).

## Logs

- SLF4J. Mensajes de negocio en español, en tercera persona y con identificadores: `log.info("Cita {} reservada para DNI {} en {}", codigo, dniEnmascarado, oficina)`.
- Enmascarar DNI en logs (`4000****`). Nunca registrar correos completos ni datos sensibles.

## Estilo

- Nombres de dominio en español (`Cita`, `reservar()`, `cupoDisponible()`); sufijos técnicos estándar en inglés aceptados (`Controller`, `Service`, `Port`, `Adapter`, `Dto`).
- Métodos cortos; sin lógica en constructores; inyección por constructor.
- Sin código comentado, sin `TODO` en `main` (en `main` los puntos de extensión de asignaciones se señalan solo en `docs/asignaciones/`).
