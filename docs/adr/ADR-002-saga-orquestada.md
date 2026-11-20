# ADR-002: Coordinación del flujo Solicitud → Pago → Emisión mediante Saga Orquestada en `tramites-service`

- **Estado:** Aceptado
- **Fecha:** 2026-11-18
- **Módulo / Contexto:** M3 – Gestión de Datos (`m3-datos`) y M4 (`m4-observabilidad`) / Ciclo de Vida del Trámite Consular

---

## 1. Contexto

En la arquitectura distribuida de la plataforma consular (M3–M4), el ciclo de vida de un trámite atraviesa tres contextos acotados con esquemas de base de datos aislados (*Database-per-Service*):
1. **`tramites-service` (`esquema tramites`):** registra la solicitud del ciudadano (`PENDIENTE_PAGO`) y actualiza su estado final (`EMITIDO` o `RECHAZADO`).
2. **`pagos-service` (`esquema pagos`):** procesa de manera idempotente el cobro arancelario (`CONFIRMADO` o `ANULADO`).
3. **`emision-service` (`esquema emision`):** genera y firma electrónicamente el documento consular (`DOC-YYYY-NNNNNN`).

Dado que cada servicio posee su propio esquema y usuario de base de datos y se comunica asíncronamente por RabbitMQ, no es viable utilizar transacciones distribuidas con bloqueo en dos fases (**2PC / XA**), ya que acoplarían temporalmente la disponibilidad de los tres servicios y degradarían el throughput. Necesitamos elegir entre una **Saga Orquestada** y una **Saga Coreografiada** para coordinar las transacciones locales y sus compensaciones.

---

## 2. Decisión

Adoptamos una **Saga Orquestada** centralizada en `SagaSolicitudTramite` dentro de `tramites-service` (con persistencia de estado en `tramites.saga_instancia`), reservando la **coreografía** únicamente para efectos secundarios transversales sin impacto en la máquina de estados (como el envío de avisos en `notificaciones-service`):

1. **Inicio (`ESPERANDO_PAGO`):** `POST /api/tramites` crea el trámite en `PENDIENTE_PAGO` y registra la instancia de saga.
2. **Avance a emisión (`EMISION_SOLICITADA`):** Al recibir `pago.confirmado`, `SagaSolicitudTramite.alConfirmarPago` asocia el pago, transiciona el trámite a `EN_EMISION` y encola el comando `emision.solicitar` en `tramites.outbox`.
3. **Culminación feliz (`EMISION_COMPLETADA`):** Al recibir `emision.completada`, `SagaSolicitudTramite.alCompletarEmision` registra `TramiteEmitido` (`EMITIDO`) con el número de documento.
4. **Compensación semántica ante fallo (`COMPENSADA`, Asignación A3):** Al recibir `emision.fallida`, `SagaSolicitudTramite.alFallarEmision` invoca la acción compensatoria `POST /api/pagos/{id}/anulacion` y transiciona el trámite a `RECHAZADO` con su `motivo_rechazo`.

---

## 3. Alternativas Consideradas

| Alternativa | Ventajas | Motivo de descarte |
| --- | --- | --- |
| **1. Saga Orquestada en `tramites-service` (Elegida)** | Un único punto de lectura del flujo y de las reglas de compensación (`SagaSolicitudTramite`), estado explícito auditable en `tramites.saga_instancia`, spans de trazabilidad claros (`@WithSpan` con `saga.paso`) y ausencia de dependencias cíclicas entre `pagos-service` y `emision-service`. | Concentra la lógica de coordinación del proceso en `tramites-service`. |
| **2. Saga Coreografiada pura entre los tres servicios** | Sin coordinador central; cada servicio reacciona a eventos del otro (`pagos` → `emision` → `pagos` → `tramites`). | Dispersa la máquina de estados en tres bases de código, crea acoplamiento circular cuando `emision-service` falla y debe revertir a `pagos-service` y `tramites-service`, y dificulta la depuración de bloqueos en producción. |
| **3. Transacción Distribuida 2PC (XA)** | Atomicidad inmediata tipo ACID entre múltiples recursos. | Incompatible con el desacoplamiento temporal de microservicios y RabbitMQ; introduce bloqueos prolongados y un punto único de fallo en el coordinador XA. |

---

## 4. Consecuencias y Evidencia

- **Positivas:**
  - `pagos-service` y `emision-service` no se conocen entre sí; ambos interactúan únicamente con sus propios datos y con los eventos/comandos coordinados por `tramites-service`.
  - La tabla `tramites.saga_instancia` y los spans `@WithSpan` permiten inspeccionar en todo momento en qué paso se encuentra cada trámite.
- **Negativas / Riesgos aceptados:**
  - Consistencia eventual entre los pasos (visible en el frontend durante el sondeo cada 2 s) y necesidad de que todos los pasos y compensaciones sean **idempotentes**.
- **Evidencia en el código:**
  - `m3-datos/tramites-service/src/main/java/pe/utec/consular/tramites/application/saga/SagaSolicitudTramite.java` (y su equivalente instrumentado en `m4-observabilidad`).

