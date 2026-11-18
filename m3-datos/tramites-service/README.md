# `tramites-service` — Patrones Creacionales (Factory Method y Builder), Eventos y gRPC vs. REST

Microservicio hexagonal del **Módulo 2 (`m2-integracion`)** responsable del ciclo de vida inicial de los trámites consulares (`LEGALIZACION`, `APOSTILLA` y `REGISTRO_CONSULAR`).

---

## 1. Conceptos del Curso Cubiertos

| Sesión / Sección | Concepto | Ubicación en el código |
| --- | --- | --- |
| **S2 (2.1)** | **Patrón Builder**: construcción paso a paso de un objeto inmutable con validaciones de coherencia por tipo en `build()` | `domain/Solicitud.java` |
| **S2 (2.1)** | **Patrón Factory Method**: creación polimórfica de `Legalizacion`, `Apostilla` y `RegistroConsular` sin condicionales `switch`/`instanceof` en la capa de aplicación | `domain/TramiteFactory.java`, `domain/Tramite.java` y subtipos |
| **S3 (2.3)** | **REST vs. gRPC**: la misma consulta (`ConsultarTramiteUseCase`) expuesta simultáneamente en JSON sobre HTTP/1.1 (`:8082`) y en Protocol Buffers sobre HTTP/2 (`:9082`) | `adapter/in/web/TramiteController.java` y `adapter/in/grpc/TramitesGrpcService.java` |
| **S3 (2.3)** | **Eventos de integración en RabbitMQ**: publicación de `tramite.registrado` en el exchange `consular.eventos` | `adapter/out/messaging/RabbitPublicadorDeEventos.java` |
| **S4 (2.5)** | **Simulación de fallas intermitentes**: filtro HTTP que inyecta respuestas `503` según `FALLAS_INTERMITENTES_PORCENTAJE` para las pruebas de reintentos con el sidecar Envoy | `adapter/in/web/FallasIntermitentesFilter.java` |

---

## 2. Elección Técnica para gRPC (`grpc-java` + `protobuf-maven-plugin`)

Para exponer el servicio `consular.tramites.v1.TramitesService/ConsultarTramite` en el puerto `9082` coexistiendo con Spring WebMVC en el puerto `8082`:

- Se utiliza **`io.grpc:grpc-netty-shaded` (`1.68.2`)** junto con **`protobuf-maven-plugin`** y **`os-maven-plugin`**.
- **Justificación:** Compila `src/main/proto/tramites.proto` durante la fase `generate-sources` de Maven sin requerir herramientas externas instaladas en el host, mantiene cero conflictos de dependencias con `spring-boot-starter-web` y `springdoc-openapi`, y su ciclo de vida se gestiona limpiamente mediante un componente `SmartLifecycle` (`GrpcServerLifecycle`) que arranca y detiene el servidor Netty en el puerto configurado (`grpc.port`, por defecto `9082`, o puerto efímero `0` en pruebas de integración).

---

## 3. Puertos y Endpoints

| Protocolo | Puerto | Ruta / Servicio | Descripción |
| --- | --- | --- | --- |
| **HTTP REST** | `8082` | `POST /api/tramites` | Registra un trámite (`LEGALIZACION`, `APOSTILLA`, `REGISTRO_CONSULAR`), lo transiciona a `PENDIENTE_PAGO` y publica `tramite.registrado` |
| **HTTP REST** | `8082` | `GET /api/tramites/{id}` | Consulta un trámite por su UUID |
| **HTTP REST** | `8082` | `GET /api/tramites?dni=XXXXXXXX` | Lista los trámites de un ciudadano por DNI |
| **HTTP REST** | `8082` | `/swagger-ui.html` | Documentación interactiva OpenAPI 3 |
| **gRPC** | `9082` | `consular.tramites.v1.TramitesService/ConsultarTramite` | Consulta binaria tipada del mismo trámite por UUID |

