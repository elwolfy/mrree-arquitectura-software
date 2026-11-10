# Prompt 08-7 — M2: verificación integral, frontend en modo m2 y asignación A2

> Rama `main` y luego rama `solucion/a2`. Referencias: contrato (secciones 6, 6.1, asignaciones), reglas 05 y 06, workflows `/verificar-modulo` y `/crear-solucion-asignacion`. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Parte A — Frontend y verificación (rama `main`)

1. Agregar `frontend` al compose de M2 con `MODULO=m2`, `API_BASE_URL=http://localhost:8080`, `FUNC_TRAMITES=true`, `FUNC_BFF=true` (`config.json` en tiempo de ejecución). La pantalla "Mi espacio" usa `/bff/inicio` y muestra los avisos de degradación; "Iniciar trámite" usa `/bff/tramites` y muestra el mensaje de identidad no vigente o no encontrada.
2. Ejecutar `/verificar-modulo m2-integracion` y corregir lo necesario.
3. Recorrer los criterios de aceptación de los pasos 08-1 a 08-6 y registrar la evidencia (comandos y salidas) en el *Walkthrough*.
4. Verificar la coherencia con el material del curso: existen exactamente estas rutas, que la presentación y el material de estudio del Módulo 2 citan:
   - `m2-integracion/tramites-service/src/main/java/pe/utec/consular/tramites/domain/TramiteFactory.java` y `Solicitud.java`
   - `m2-integracion/identidad-service/src/main/java/pe/utec/consular/identidad/adapter/out/pide/ReniecPideAdapter.java`
   - `m2-integracion/bff-web/src/main/java/pe/utec/consular/bff/application/TramiteFacade.java` e `InicioCiudadanoService.java`
   - `m2-integracion/gateway/src/main/java/pe/utec/consular/gateway/CorrelationIdFilter.java` y `src/main/resources/application.yml`
   - `m2-integracion/notificaciones-service/.../adapter/in/messaging/TramiteRegistradoConsumer.java`
   - `m2-integracion/tramites-service/.../application/port/out/PublicadorDeEventosPort.java`
   - `m2-integracion/tramites-service/src/main/proto/tramites.proto`, `m2-integracion/envoy/envoy.yaml`, `m2-integracion/pide-simulador/mappings/`
   - `m2-integracion/scripts/levantar.ps1`, `m2-integracion/scripts/grpc-consultar.ps1`
   - `docs/ddd/lenguaje-ubicuo.md`, `docs/ddd/context-map.md`, `docs/contratos/asyncapi.yaml`, `docs/pruebas/colecciones/m2.postman_collection.json`
   - En M1 (laboratorio de S2): `m1-estilos/monolito-modular/.../tramites/domain/tarifa/TarifaStrategy.java` y `CalculadoraDeTarifas.java`, sin `RegistroConsularTarifaStrategy`.
5. Crear `docs/asignaciones/A2.md` (guía del participante, 30 min): contexto, tareas, puntos de partida en el código, criterios de aceptación y entrega en la rama `a2-<usuario>` del fork. **Sin la solución.**
6. Commit en `main`: `feat(m2): verificación integral y guía de A2`. Etiqueta `s3`.

## Parte B — Solución de referencia de A2 (rama `solucion/a2`)

Ejecutar `/crear-solucion-asignacion A2` con este alcance (contrato, sección de asignaciones):

- tramites-service: `PUT /api/tramites/{id}/observacion` (`{motivo}`) → estado `OBSERVADO`; publica `tramite.observado` (`{tramiteId, codigo, dni, email, motivo}`) por `PublicadorDeEventosPort`.
- notificaciones-service: `TramiteObservadoConsumer` en la cola `notificaciones.tramite.observado`, idempotente (tabla `mensaje_procesado`) y con DLQ, siguiendo el modelo de `TramiteRegistradoConsumer`; envía el aviso "Su trámite requiere una corrección".
- `GET /api/tramites/observados` en tramites-service y su ruta en el gateway.
- `rabbitmq/definitions.json` y `docs/contratos/asyncapi.yaml` actualizados.
- Pruebas: integración con RabbitMQ (evento publicado y consumido una sola vez aunque se duplique).
- `docs/asignaciones/A2-solucion.md` con la explicación de las decisiones (en qué bounded context ocurre el hecho, por qué es asíncrono, relación conformista).

**La rama `main` no debe contener ningún archivo de la solución.** Verificar con `git diff main..solucion/a2 --stat`.

## Criterios de aceptación

- `main`: `git grep -n "tramite.observado\|observados" -- ':!docs/asignaciones/A2.md' ':!docs/especificacion'` no devuelve resultados.
- `solucion/a2`: desde Swagger del gateway, observar un trámite → mensaje en `notificaciones.tramite.observado`, un correo en Mailpit, y el trámite aparece en `GET localhost:8080/api/tramites/observados`.

## Commit

`main`: `feat(m2): verificación integral y guía de A2` · `solucion/a2`: `sol(a2): evento tramite.observado entre contextos`
