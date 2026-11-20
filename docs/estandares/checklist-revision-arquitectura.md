# Lista de Verificación (Checklist) para Revisión de Arquitectura y Pull Requests

> **Gobernanza Técnica — Plataforma Digital de Servicios Consulares y de Legalización**
> Todo cambio arquitectónico, nueva funcionalidad o entrega del Proyecto Final debe superar esta lista de verificación antes de integrarse.

---

## 1. Atributos de Calidad y Diseño Estructural

- [ ] **Fronteras Hexagonales / Clean:** `mvn test` pasa con todas las pruebas de **ArchUnit** (`ReglasDeArquitecturaTest`) en verde; el paquete `domain` no importa clases de Spring, JPA, Jackson ni clientes HTTP/AMQP.
- [ ] **Aislamiento de Datos (*Database-per-Service*):** Cada microservicio accede exclusivamente a su propio esquema de PostgreSQL (`citas`, `tramites`, `pagos`, `emision`, `notificaciones`) con su usuario dedicado (`<esquema>_app`); no existen `JOIN` SQL entre esquemas de distintos servicios.
- [ ] **Resiliencia y Disponibilidad:** Los contenedores definen `healthcheck`, `restart: unless-stopped` y `mem_limit`; el pool HikariCP configura `connection-timeout: 2000` y `validation-timeout: 1000`.
- [ ] **Rendimiento y Escalabilidad:** Las consultas frecuentes están respaldadas por índices declarados y comentados en migraciones Flyway (o por caché *Cache-Aside* con TTL e invalidación activa) para sostener el SLO ($< 500\text{ ms}$ al 99 %).

---

## 2. Seguridad y Gestión de Secretos

- [ ] **Contenedores sin privilegios de `root`:** Los `Dockerfile` crean y utilizan un usuario sin privilegios (`USER app`).
- [ ] **Ausencia de credenciales reales:** Ningún archivo de código, propiedad ni script contiene claves reales, tokens privados ni datos de ciudadanos reales (solo datos ficticios de laboratorio).
- [ ] **Validación de entrada y errores seguros:** Todos los DTOs de entrada usan Jakarta Validation (`@Valid`, `@NotBlank`, `@Pattern`) y los errores se devuelven en formato **RFC 9457 (`ProblemDetail`)** sin exponer stack traces ni nombres internos de tablas.
- [ ] **Protección de borde:** Los endpoints expuestos a través del API Gateway respetan la política de CORS y, cuando invocan servicios externos sensibles como la PIDE, aplican Rate Limiting (`429 Too Many Requests`).

---

## 3. Protección de Datos Personales (Ley N.° 29733 y D.L. N.° 1412)

- [ ] **Enmascaramiento en Logs:** El número de DNI se registra siempre enmascarado en los logs (`****0001`), nunca los 8 dígitos completos.
- [ ] **Minimización de datos en Logs y Trazas:** No se registran direcciones de correo electrónico completas ni contenido sensible de documentos en líneas de log ni en atributos de spans de OpenTelemetry.

---

## 4. Integración, Mensajería y Consistencia Eventual

- [ ] **Gobernanza de APIs REST:** El contrato OpenAPI (`/v3/api-docs`) supera sin errores la validación de **Spectral** (`.\scripts\validar-apis.ps1`), cumpliendo rutas en `kebab-case` (`^(/[a-z0-9-{}]+)+$`).
- [ ] **Interoperabilidad PIDE desacoplada:** Toda consulta a servicios externos del Estado (RENIEC/PIDE) atraviesa una **Capa Anticorrupción (ACL)** con timeout, reintentos acotados y Circuit Breaker (Resilience4j).
- [ ] **Prevención de Escritura Doble (*Dual Write*):** Los servicios que actualizan PostgreSQL y publican eventos críticos de negocio utilizan **Transactional Outbox** (`outbox` + `RelayOutbox`).
- [ ] **Idempotencia de Extremo a Extremo:**
  - Los endpoints de cobro o mutación crítica exigen `Idempotency-Key`.
  - Los consumidores de RabbitMQ verifican `messageId` en su tabla `mensaje_procesado` antes de ejecutar efectos secundarios y configuran reintentos exponenciales con cola **DLQ**.

---

## 5. Observabilidad y Documentación Viva

- [ ] **Trazabilidad Distribuida:** La operación propaga `X-Correlation-Id` y `traceparent` (incluyendo tramos asíncronos por RabbitMQ/Outbox) y es visible de principio a fin en **Grafana Tempo**.
- [ ] **Métricas y Logs JSON:** El servicio emite logs en formato JSON con `trace_id` y `span_id` hacia **Loki** y registra métricas de negocio en **Micrometer / Prometheus**.
- [ ] **ADR y Modelo C4:** Las decisiones de diseño relevantes están documentadas en `docs/adr/ADR-NNN-*.md` y cualquier nuevo contenedor, componente o almacén de datos (como Redis) está reflejado y validado en `docs/c4/workspace.dsl`.

