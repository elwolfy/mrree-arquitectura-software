# Prompt 08-5 — M2: `pagos-service` (Strategy), `bff-web` (Facade) y `gateway`

> Rama `main`. Referencias: contrato (sección 6), reglas 01–05. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Objetivo didáctico

Completar el borde del sistema (2.2): un API Gateway como entrada única, un BFF que agrega y adapta para la web, y el servicio de tarifas con el patrón Strategy (2.1).

## Alcance

1. **`pagos-service`** (8083, `pe.utec.consular.pagos`):
   - En M2 solo `GET /api/tarifas?tipoTramite=&pais=` (contrato).
   - `domain/tarifa/TarifaStrategy.java` con `LegalizacionTarifaStrategy`, `ApostillaTarifaStrategy` y **`RegistroConsularTarifaStrategy` ya incluida** (en M1 se agrega en clase; en M2 forma parte de la plataforma). Inyección como `List<TarifaStrategy>` y `CalculadoraDeTarifas` sin `switch`.
   - Modelo propio `OrdenDePago` (solo la clase de dominio en M2) distinto de `Solicitud` de trámites: comentario que remita a `docs/ddd/lenguaje-ubicuo.md`.
2. **`bff-web`** (8085, `pe.utec.consular.bff`):
   - `GET /bff/inicio?dni=` en `application/InicioCiudadanoService.java`: consulta en paralelo (hilos virtuales o `CompletableFuture`) citas, trámites y avisos; si un servicio falla o excede 2 s, devuelve esa sección vacía y un arreglo `avisosDeDegradacion` con el motivo.
   - `POST /bff/tramites` en `application/TramiteFacade.java` (**Facade**): valida identidad → calcula tarifa → registra trámite, y responde un DTO pensado para la pantalla. Javadoc: la fachada coordina, las reglas permanecen en los servicios.
   - Clientes HTTP declarativos (`@HttpExchange`) hacia los servicios **por nombre de contenedor**, no a través del gateway.
3. **`gateway`** (8080, Spring Cloud Gateway, variante Server MVC o WebFlux compatible con la versión de Spring Boot fijada):
   - Rutas del contrato en `src/main/resources/application.yml`: `/api/citas/**`, `/api/tramites/**`, `/api/tarifas/**`, `/api/avisos/**`, `/api/identidad/**`, `/bff/**`.
   - `CorrelationIdFilter.java`: agrega `X-Correlation-Id` (UUID) si no existe y lo propaga; todos los servicios lo registran en el MDC y en el patrón de log.
   - Límite de solicitudes simple en `/api/identidad/**` (por ejemplo 5 por segundo), respondiendo `429`.
   - CORS para `http://localhost:4200`.
   - Swagger agregado en `localhost:8080/swagger-ui.html` (springdoc `urls` hacia los documentos de cada servicio).
   - **Sin lógica de negocio**: README del gateway con la sección "Riesgo: monolito en el borde".
4. Agregar los tres al compose.

## Pruebas

- `CalculadoraDeTarifasTest` con los tres tipos.
- `InicioCiudadanoServiceTest` con un servicio simulado caído → respuesta parcial con aviso.
- Gateway: prueba de integración que verifica la presencia de `X-Correlation-Id` y el `429`.

## Criterios de aceptación

- `curl localhost:8080/bff/inicio?dni=40000001` responde las tres secciones; con `docker stop m2-notificaciones` responde avisos vacíos y un aviso de degradación, sin error.
- El mismo `X-Correlation-Id` aparece en los logs de `m2-gateway`, `m2-bff`, `m2-identidad` y `m2-tramites` al iniciar un trámite por `POST /bff/tramites`.

## Commit

`feat(m2): pagos con Strategy, bff-web con Facade y API Gateway`
