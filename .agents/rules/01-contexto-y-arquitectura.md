---
description: Contexto del curso y reglas de arquitectura para todo el repositorio
activation: always
---

# Contexto y arquitectura

## Propósito del código

Este repositorio es **material de enseñanza**. Su calidad se mide por lo claro que muestra cada concepto del curso y por su facilidad para ejecutarse en un equipo Windows con Docker Desktop, no por la cantidad de funcionalidades. Preferir código explícito y legible sobre abstracciones ingeniosas. Evitar dependencias que no se usen en clase.

Referencia obligatoria: `@docs/especificacion/contrato-global.md`.

## Mapa de conceptos → código

| Sección del plan | Concepto | Dónde debe verse en el código |
| --- | --- | --- |
| 1.1 | Atributos de calidad | Health checks, `restart` en compose, réplicas escalables, pruebas ArchUnit |
| 1.2 | Monolito / microservicios / serverless | `m1-estilos/monolito-modular`, `microservicios`, `serverless` con la misma API |
| 1.3 | Hexagonal vs. Clean | `m1-estilos/monolito-modular/.../citas` vs. `m1-estilos/comparativa-clean/.../citas` |
| 1.4 | CAP / PACELC | `consistencia.modo` CP/AP en citas (monolito y clean) |
| 2.1 | Patrones GoF | Factory, Builder, Adapter, Facade, Strategy, Observer (ver tabla del contrato y prompt 08) |
| 2.2 | Gateway, BFF, Sidecar | `m2-integracion/gateway`, `bff-web`, `envoy` |
| 2.3 | REST, gRPC, broker | REST en todos, gRPC en tramites, RabbitMQ |
| 2.4 | DDD | Paquetes por bounded context, ACL en identidad, glosario en `docs/` |
| 3.1 | SQL vs. NoSQL | Esquemas PostgreSQL, `JSONB` en expediente, Redis |
| 3.2 | CQRS / Event Sourcing | `eventos_tramite`, `vista_tramite`, proyector |
| 3.3 | Saga, Outbox, idempotencia | Orquestador en tramites, `outbox`, `Idempotency-Key` |
| 3.4 | Caché y optimización | Índices, script de carga masiva, keyset pagination; Redis en A4 |
| 4.1 | C4 | `docs/c4/workspace.dsl` |
| 4.2 | Observabilidad | OpenTelemetry, Grafana LGTM, logs JSON con `trace_id` |
| 4.3 | Deuda técnica | Perfil `calidad` con SonarQube; ejemplos de Strangler Fig documentados |
| 4.4 | Gobernanza | `docs/estandares/`, ArchUnit en CI (GitHub Actions) |

## Arquitectura hexagonal (regla general de servicios)

Estructura de paquetes por bounded context:

```text
pe.utec.consular.<contexto>
├── domain/                     Entidades, objetos de valor, reglas, eventos de dominio. Sin Spring, sin JPA.
├── application/
│   ├── port/in/                Casos de uso (interfaces) — <Verbo><Sustantivo>UseCase
│   ├── port/out/               Contratos hacia infraestructura — <Sustantivo>Port / <Sustantivo>RepositoryPort
│   └── service/                Implementación de casos de uso — <Verbo><Sustantivo>Service
└── adapter/
    ├── in/web/                 Controladores REST, DTO, mapeadores
    ├── in/messaging/           Consumidores de mensajes
    ├── in/grpc/                Servicios gRPC
    └── out/persistence|messaging|http|…   Implementaciones de puertos de salida
```

Reglas de dependencia (verificadas con ArchUnit):

- `domain` no depende de ningún otro paquete del proyecto ni de `org.springframework..`, `jakarta.persistence..`, `com.fasterxml..`.
- `application` depende solo de `domain` (puede usar `org.springframework.transaction.annotation` y `org.springframework.stereotype` solo en `service`).
- `adapter` depende de `application` y `domain`; los adaptadores no se llaman entre sí.
- Las entidades JPA viven en `adapter.out.persistence` (`<Sustantivo>JpaEntity`) y se mapean a/desde el dominio. **Nunca** exponer entidades JPA en controladores.

## Clean Architecture (solo `m1-estilos/comparativa-clean`)

```text
pe.utec.consular.clean.citas
├── entities/                   Cita, Cupo, OficinaConsular (reglas empresariales)
├── usecase/                    RegistrarCitaInputBoundary, RegistrarCitaInteractor, RegistrarCitaOutputBoundary,
│                               CitaGateway, modelos de petición/respuesta
├── interfaceadapters/
│   ├── controller/             CitaController
│   ├── presenter/              CitaPresenter, CitaViewModel
│   └── gateway/                CitaJpaGateway
└── frameworks/                 Configuración Spring, JPA (entidades y repositorios), web
```

Las dependencias apuntan solo hacia adentro: `frameworks → interfaceadapters → usecase → entities`.

## Monolito modular

Cada módulo (`citas`, `tramites`, `notificaciones`) es un paquete de primer nivel bajo `pe.utec.consular.monolito`. La comunicación entre módulos pasa solo por su API pública (paquete raíz del módulo o `api/`), verificado con Spring Modulith (`ApplicationModules.of(...).verify()`). Cada módulo usa su propio esquema de base de datos.

## Decisiones de diseño que no se cambian sin ADR

- PostgreSQL como única base relacional; Redis como clave-valor; RabbitMQ como broker.
- Una carpeta de módulo = un `docker-compose.yml` autosuficiente.
- La misma API pública en todas las variantes de M1.
- Saga **orquestada** en tramites-service para el flujo solicitud → pago → emisión; coreografía solo para notificaciones.

## Comentarios didácticos

Formato obligatorio en clases que ilustran un concepto:

```java
/**
 * Puerto de entrada para registrar una cita.
 * <p>Concepto: Arquitectura hexagonal – puerto primario (sección 1.3 del plan).
 * <p>Sesión: S2. Compárese con {@code RegistrarCitaInputBoundary} en comparativa-clean.
 */
```

No llenar el código de comentarios triviales; solo donde aportan al aprendizaje.
