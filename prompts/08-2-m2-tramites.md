# Prompt 08-2 — M2: `tramites-service` (Factory Method, Builder, eventos y gRPC)

> Rama `main`. Referencias: contrato (secciones 1, 6, 6.1 y el `.proto` de la sección 6), reglas 01–04. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Objetivo didáctico

Servicio que materializa dos patrones creacionales (2.1) en el dominio y expone la misma consulta por REST y por gRPC para compararlos (2.3).

## Alcance

`m2-integracion/tramites-service`, puertos 8082 (HTTP) y 9082 (gRPC), paquete `pe.utec.consular.tramites`, hexagonal.

1. **Dominio** (`domain/`), sin dependencias de Spring:
   - Abstracción `Tramite` y subtipos `Legalizacion`, `Apostilla`, `RegistroConsular`, cada uno con sus datos y validaciones propias.
   - **Factory Method**: `domain/TramiteFactory.java` con `crear(TipoTramite tipo, Solicitud solicitud)`. Javadoc que explique el patrón, el problema que resuelve y su costo.
   - **Builder**: `domain/Solicitud.java` inmutable con `Solicitud.builder().dni(..).tipo(..).pais(..).folios(..).idioma(..).build()`; `build()` valida coherencia por tipo y lanza una excepción de dominio clara.
   - Estados en M2: `REGISTRADO` → `PENDIENTE_PAGO` (el pago llega en M3). `OBSERVADO` existe en el enum del contrato pero **no tiene transición ni endpoint en `main`** (A2).
2. **Aplicación**: casos de uso `RegistrarTramite`, `ConsultarTramite`, `ListarTramitesPorDni`. Puerto de salida `application/port/out/PublicadorDeEventosPort.java` (mismo nombre que en citas; implementación propia, sin compartir código).
3. **Adaptadores**:
   - REST: `POST /api/tramites`, `GET /api/tramites/{id}`, `GET /api/tramites?dni=` (contrato 6).
   - Mensajería: publica `tramite.registrado` (contrato 6.1) al registrar.
   - **gRPC**: `src/main/proto/tramites.proto` con el servicio `consular.tramites.v1.TramitesService/ConsultarTramite` del contrato; servidor en 9082 (Spring gRPC o grpc-java con `protobuf-maven-plugin`; documentar la elección en el README).
   - Persistencia JPA en el esquema `tramites` con Flyway.
4. **Fallas intermitentes didácticas**: propiedad `fallas.intermitentes.porcentaje` (por defecto 0, variable `FALLAS_INTERMITENTES_PORCENTAJE`). Un filtro HTTP responde `503` en ese porcentaje de solicitudes `GET`. Se usa en 08-6 con el sidecar.
5. Agregar el servicio al compose (`m2-tramites`, puertos 8082 y 9082).

## Pruebas

- Unitarias: `TramiteFactoryTest` (un caso por tipo y tipo nulo), `SolicitudBuilderTest` (datos obligatorios faltantes, coherencia por tipo, inmutabilidad).
- ArchUnit: `domain` no depende de Spring ni de `adapter`.
- Integración (Testcontainers PostgreSQL + RabbitMQ): registrar publica `tramite.registrado`; gRPC `ConsultarTramite` devuelve el mismo trámite que REST.

## Criterios de aceptación

- `POST /api/tramites` con los tres tipos crea el subtipo correcto (visible en la respuesta) y deja el trámite en `PENDIENTE_PAGO`.
- `git grep -n "switch\|instanceof" tramites-service/src/main/java/**/application` no muestra decisiones por tipo fuera de la fábrica.
- `mvn -f m2-integracion verify` pasa.

## Commit

`feat(m2): tramites-service con Factory Method, Builder y gRPC`
