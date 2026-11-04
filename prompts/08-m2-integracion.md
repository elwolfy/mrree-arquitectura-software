# Prompt 08 — M2: Integración, comunicación y DDD

> Rama `main`. Referencias: contrato (secciones 1, 3, 6, 6.1), reglas 01–05, plan sección 7 (2.1–2.4) y sección 8 (S3).

## Objetivo didáctico

Plataforma con varios bounded contexts comunicados por REST, gRPC y eventos, detrás de un API Gateway y un BFF, con una capa anticorrupción hacia la PIDE simulada y un sidecar opcional.

## Servicios (`m2-integracion/`, compose `name: m2`)

| Servicio | Puerto | Contenido clave |
| --- | --- | --- |
| `gateway` | 8080 | Spring Cloud Gateway (variante MVC o WebFlux compatible). Rutas del contrato (sección 6). CORS para `localhost:4200`. Límite de solicitudes simple en `/api/identidad/**`. Filtro que agrega `X-Correlation-Id`. **Sin lógica de negocio** (documentarlo como riesgo en el README). |
| `bff-web` | 8085 | `GET /bff/inicio?dni=` agrega citas + trámites + avisos en paralelo (hilos virtuales / `CompletableFuture`), tolera fallas parciales (sección vacía + aviso). `POST /bff/tramites` = **Facade** (`TramiteFacade`): valida identidad → calcula tarifa → crea trámite. |
| `citas-service` | 8081 | Citas del monolito de M1 como servicio (hexagonal). Publica `cita.reservada`. |
| `tramites-service` | 8082 / 9082 | `POST /api/tramites`, `GET /api/tramites/{id}`, `GET /api/tramites?dni=`. **Factory Method** (`TramiteFactory` según `TipoTramite`) y **Builder** (`Solicitud.builder()`). Publica `tramite.registrado`. Servidor **gRPC** `ConsultarTramite` (proto del contrato). Estados en M2: `REGISTRADO` → `PENDIENTE_PAGO` (el pago llega en M3). |
| `pagos-service` | 8083 | En M2 solo `GET /api/tarifas` con **Strategy** (`TarifaStrategy`, igual que M1 + `REGISTRO_CONSULAR` ya incluido). |
| `notificaciones-service` | 8084 | Consume `cita.reservada` y `tramite.registrado` (**Observer** vía broker), idempotente, envía a Mailpit; `GET /api/avisos?dni=`. |
| `identidad-service` | 8086 | `POST /api/identidad/validaciones {dni}`. **Adapter** (`ReniecPideAdapter`) + **capa anticorrupción**: traduce el modelo de la PIDE (`coResultado`, `datosPersona.prenombres`, …, en formato distinto y con nombres ajenos) al modelo propio `Ciudadano`. Documentar el ACL en Javadoc y README. |
| `pide-simulador` | 8089 | WireMock con mappings: DNI `40000001`–`40000050` válidos (nombres ficticios), `49999999` fallecido, `48888888` no encontrado, retardo de 300 ms; mapping con falla 503 activable por escenario WireMock. |
| `rabbitmq` | 5672/15672 | Exchange `consular.eventos`, colas y DLQ del contrato declaradas por definiciones (`definitions.json`) o por los propios servicios. |
| `postgres`, `mailpit`, `frontend` | — | Frontend con `MODULO=m2`, `API_BASE_URL=http://localhost:8080`, `FUNC_TRAMITES=true`, `FUNC_BFF=true`. |
| `envoy` (perfil `sidecar`) | 9901 | Sidecar delante de `tramites-service`: el gateway enruta a `envoy:10000` → `tramites:8082`, con política de reintentos (3) y timeout. Demostración de reintentos sin cambiar código: `tramites-service` tiene propiedad `fallas.intermitentes.porcentaje` (por defecto 0). |

### DDD

- Paquetes por bounded context, glosario en `docs/ddd/lenguaje-ubicuo.md` (tabla del contrato 1.1) y `docs/ddd/context-map.md` con el mapa (Mermaid) y relaciones: Citas → Trámites (proveedor), Trámites ↔ Pagos (cliente-proveedor), Identidad (ACL frente a RENIEC/PIDE), Notificaciones (conformista, published language).
- Mostrar en el código que "Solicitud" en tramites y "orden de pago" en pagos son modelos distintos.

### Contratos

- `docs/contratos/asyncapi.yaml` con los eventos del contrato 6.1.
- OpenAPI de cada servicio publicado por springdoc; el gateway agrega en `/swagger-ui.html` los documentos de los servicios (springdoc `urls`).
- Colección `docs/pruebas/colecciones/m2.postman_collection.json` (y Bruno): flujo completo, gRPC no incluido.
- Script `scripts/grpc-consultar.(ps1|sh)` que usa `fullstorydev/grpcurl` en contenedor contra `m2-tramites:9082` y compara el tamaño de la respuesta con la de REST.

### Pruebas

- ArchUnit en todos los servicios.
- Integración con Testcontainers RabbitMQ: publicar `tramite.registrado` → notificaciones lo procesa una sola vez aunque el mensaje se duplique.
- identidad-service: WireMock en prueba para los tres casos de DNI.

### README de `m2-integracion`

Diagrama Mermaid de contenedores, experimentos de S3 (seguir una petición frontend → BFF → gateway → servicio con `X-Correlation-Id` en logs; DNI válido/ inválido; detener notificaciones y observar la cola en RabbitMQ; REST vs gRPC; perfil `sidecar` con fallas intermitentes), modo híbrido, memoria medida.

## Criterios de aceptación

- `/verificar-modulo m2-integracion` pasa.
- Desde el frontend: iniciar trámite con DNI `40000001` → trámite `PENDIENTE_PAGO`, aviso visible en Mailpit; con `49999999` → mensaje de identidad no válida.
- `docker stop m2-notificaciones`, crear 3 trámites, ver 3 mensajes en la cola (consola RabbitMQ), `docker start m2-notificaciones` → 3 avisos y cola vacía.
- `scripts/grpc-consultar` muestra la respuesta gRPC y la comparación de tamaño.
- Con perfil `sidecar` y `FALLAS_INTERMITENTES_PORCENTAJE=50`, las consultas a través del gateway responden correctamente gracias a los reintentos de Envoy (evidencia en las estadísticas de Envoy en `:9901/stats`).
- No existe `tramite.observado` ni `/api/tramites/observados` (A2).

## Commit

`feat(m2): integración y comunicación`
