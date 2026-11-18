# Prompt 09-6 — M3: verificación integral, guía de A3 y solución de referencia

> Rama `main` y luego rama `solucion/a3`. Referencias: contrato (sección 8), reglas 04 y 06, workflows `/verificar-modulo` y `/crear-solucion-asignacion`. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación. Al finalizar vuelve a `main` y deja los contenedores detenidos.

## Parte A — `main`

1. Ejecutar `/verificar-modulo m3-datos` y corregir lo necesario.
2. Recorrer los criterios de aceptación de 09-1 a 09-5 y registrar la evidencia en el *Walkthrough*.
3. Verificar que existen exactamente estas rutas, citadas en la presentación del Módulo 3:
   - `m3-datos/postgres/initdb/01-esquemas.sql`, `m3-datos/docker-compose.yml`
   - `m3-datos/tramites-service/src/main/resources/db/migration/tramites/`
   - `m3-datos/tramites-service/src/main/java/pe/utec/consular/tramites/domain/Tramite.java`
   - `…/tramites/application/proyeccion/ProyectorVistaTramite.java`
   - `…/tramites/application/saga/SagaSolicitudTramite.java` (con `alFallarEmision` y su `TODO`)
   - `…/tramites/adapter/out/outbox/RelayOutbox.java`
   - `…/tramites/application/port/out/CacheConsultaTramitePort.java`
   - `m3-datos/pagos-service/src/main/java/pe/utec/consular/pagos/application/RegistrarPagoService.java`
   - `m3-datos/scripts/levantar.ps1`, `psql.ps1`, `pagar-dos-veces.ps1`, `cargar-datos-masivos.ps1`
   - `docs/pruebas/sql/carga-masiva.sql`, `docs/pruebas/sql/consultas-optimizacion.sql`, `docs/matrices/m3-optimizacion.md`, `docs/pruebas/colecciones/m3.postman_collection.json`
4. Crear `docs/asignaciones/A3.md` (≈ 45 min, estructura de la regla 06):
   - **Tarea 1 – conceptual (≈ 20 min):** completar en el mismo documento la tabla "Paso · Servicio · Transacción local · Compensación" de la saga (filas: registrar trámite, confirmar pago, solicitar emisión, emitir) y justificar en un párrafo por qué el flujo se orquesta en lugar de coreografiarse. La tabla va vacía.
   - **Tarea 2 – práctica (≈ 25 min):** completar `SagaSolicitudTramite.alFallarEmision` usando `AnulacionPagoPort` y el evento `TramiteRechazado`; habilitar `SagaCompensacionTest`.
   - Verificación con `EMISION_SIMULAR_FALLA=true`: pago `ANULADO`, trámite `RECHAZADO` con motivo, aviso en Mailpit.
   - Entrega en la rama `a3-<usuario>` antes de la sesión 5; rúbrica 60 % / 40 %. **Sin la solución.**
5. Commit en `main`: `feat(m3): verificación integral y guía de A3`. Etiqueta `s4`.

## Parte B — `solucion/a3`

Ejecutar `/crear-solucion-asignacion A3`, mínima y ejemplar:

- `alFallarEmision`: solicita la anulación del pago y registra `TramiteRechazado` con el motivo; idempotente ante un evento repetido. Quitar el log de advertencia y el `@Disabled`.
- `docs/asignaciones/A3.md` con la tabla de compensaciones completa y la justificación (registrar trámite ↔ rechazar; confirmar pago ↔ anular, nunca borrar; solicitar emisión y emitir sin compensación propia; orquestación por visibilidad del flujo y estado de la saga en un solo lugar).
- `docs/asignaciones/A3-solucion.md`: archivos modificados, explicación (compensación semántica, idempotencia de la compensación, qué pasa si la anulación falla) y "Cómo verificar" con salida real.

## Criterios de aceptación

- `main`: prueba de A3 omitida; con la falla activa, el trámite queda `EN_EMISION` con la advertencia.
- `solucion/a3`: con la falla activa, pago `ANULADO` y trámite `RECHAZADO` con motivo; prueba en verde; `git diff --stat main...solucion/a3` muestra solo `SagaSolicitudTramite.java`, `SagaCompensacionTest.java`, `A3.md` y `A3-solucion.md`.

## Commits

`main`: `feat(m3): verificación integral y guía de A3` · `solucion/a3`: `feat(m3): solución de referencia A3`
