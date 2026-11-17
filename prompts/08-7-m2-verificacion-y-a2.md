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
5. **Andamiaje de A2** (contrato, sección 8; regla 06):
   - tramites-service: `PUT /api/tramites/{id}/observacion` (`{motivo}`) → estado `OBSERVADO`; publica `tramite.observado` (`{tramiteId, codigo, dni, email, motivo}`) por `PublicadorDeEventosPort`. Completo.
   - `rabbitmq/definitions.json`: cola `notificaciones.tramite.observado` y su `.dlq`, enlazadas al exchange. Sin consumidor, el mensaje queda esperando en la cola (se ve en la consola).
   - notificaciones-service: plantilla del aviso "Su trámite requiere una corrección" y `src/test/java/.../TramiteObservadoConsumerTest.java` con `@Disabled("Asignación A2")` (mensaje duplicado → un solo aviso; mensaje inválido → DLQ). **Sin** la clase `TramiteObservadoConsumer`.
   - No se documenta `tramite.observado` en `docs/ddd/context-map.md` ni en `asyncapi.yaml` (tarea conceptual).
6. Crear `docs/asignaciones/A2.md` (≈ 45 min, estructura de la regla 06):
   - **Tarea 1 – conceptual (≈ 20 min):** documentar `tramite.observado` en `docs/ddd/context-map.md` (contexto dueño del hecho, por qué es asíncrono, relación del context map entre Trámites y Notificaciones) y agregarlo a `docs/contratos/asyncapi.yaml` siguiendo el formato de `tramite.registrado`.
   - **Tarea 2 – práctica (≈ 25 min):** crear `TramiteObservadoConsumer` en `adapter/in/messaging/` tomando como modelo `TramiteRegistradoConsumer`, y habilitar su prueba.
   - Verificación: observar un trámite por Swagger del gateway → el mensaje esperando en la cola → al levantar notificaciones con el consumidor, la cola queda en 0 y el aviso aparece en Mailpit.
   - Entrega en la rama `a2-<usuario>`; rúbrica 60 % / 40 %. **Sin la solución.**
7. Commit en `main`: `feat(m2): verificación integral, andamiaje y guía de A2`. Etiqueta `s3`.

## Parte B — Solución de referencia de A2 (rama `solucion/a2`)

Ejecutar `/crear-solucion-asignacion A2`, mínima y ejemplar:

- `TramiteObservadoConsumer` en notificaciones-service (cola `notificaciones.tramite.observado`, idempotente con `mensaje_procesado`, reintentos y DLQ iguales al modelo); quitar `@Disabled`.
- `docs/ddd/context-map.md` y `docs/contratos/asyncapi.yaml` con `tramite.observado` documentado (contexto Trámites, asíncrono porque el aviso no condiciona la observación, Notificaciones conformista frente al lenguaje publicado).
- `docs/asignaciones/A2-solucion.md` con archivos modificados, explicación y "Cómo verificar" con salida real.

## Criterios de aceptación

- `main`: observar un trámite deja 1 mensaje en `notificaciones.tramite.observado`; no existe `TramiteObservadoConsumer`; la prueba de A2 aparece omitida; `/verificar-modulo m2-integracion` pasa.
- `solucion/a2`: la cola queda en 0, un solo correo en Mailpit aunque el mensaje se reenvíe, prueba de A2 en verde; `git diff --stat main...solucion/a2` muestra solo el consumidor, su prueba, `context-map.md`, `asyncapi.yaml` y `A2-solucion.md`.

## Commit

`main`: `feat(m2): verificación integral, andamiaje y guía de A2` · `solucion/a2`: `sol(a2): evento tramite.observado entre contextos`
