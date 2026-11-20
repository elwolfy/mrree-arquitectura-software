# `gateway` — API Gateway de Borde (Spring Cloud Gateway MVC, `:8080`)

Punto de entrada único del **Módulo 2 (`m2-integracion`)** en `http://localhost:8080`.

---

## 1. Responsabilidades Transversales del Gateway

1. **Enrutamiento declarativo (`application.yml`):** Redirige el tráfico externo hacia `citas-service` (`:8081`), `tramites-service` (`:8082`), `pagos-service` (`:8083`), `notificaciones-service` (`:8084`), `bff-web` (`:8085`) e `identidad-service` (`:8086`).
2. **Trazabilidad (`CorrelationIdFilter`):** Genera `X-Correlation-Id` (UUID) si el cliente no lo envía, lo registra en el MDC propio y lo inyecta hacia todos los servicios aguas abajo.
3. **Protección de borde (`IdentidadRateLimitFilter`):** Limita la tasa de solicitudes hacia `/api/identidad/**` (5 solicitudes/segundo), devolviendo `429 Too Many Requests` ante ráfagas excesivas.
4. **CORS centralizado:** Habilita peticiones desde `http://localhost:4200` (Frontend Angular).
5. **Swagger UI agregado (`http://localhost:8080/swagger-ui.html`):** Centraliza la exploración de los contratos OpenAPI de todos los microservicios.

---

## 2. Riesgo: Monolito en el Borde (*Edge Monolith* / *Smart Pipes*)

Un antipatrón frecuente al introducir un API Gateway o un ESB es trasladar **reglas de negocio, validaciones de dominio o composición transaccional** dentro de los filtros del gateway (por ejemplo, validar en el gateway si un trámite requiere 1 folio, calcular recargos de tarifas o encadenar llamadas de negocio).

### Consecuencias de poner lógica de negocio en el Gateway:
- **Acoplamiento global de despliegues:** Cualquier cambio en una regla de negocio de trámites o citas obliga a recompilar y redesplegar el punto único de entrada de toda la plataforma.
- **Cuello de botella cognitivo y operativo:** El equipo de plataforma se convierte en bloqueador de todos los equipos de producto.
- **Violación del principio *Smart Endpoints and Dumb Pipes*:** La inteligencia de dominio debe vivir en los microservicios dueños del Bounded Context (`citas-service`, `tramites-service`, `pagos-service`, `identidad-service`), y la agregación específica de experiencia de usuario debe vivir en el **BFF (`bff-web`)**, dejando al API Gateway exclusivamente con preocupaciones transversales de infraestructura (enrutamiento, seguridad, correlación, CORS y *rate limiting*).

