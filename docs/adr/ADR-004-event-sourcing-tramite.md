# ADR-004: Almacenamiento del ciclo de vida del trámite mediante Event Sourcing y proyección CQRS

- **Estado:** Aceptado
- **Fecha:** 2026-11-18
- **Módulo / Contexto:** M3 – Gestión de Datos (`m3-datos`) y M4 (`m4-observabilidad`) / `tramites-service`

---

## 1. Contexto

En el dominio consular y de legalización documental, un trámite pasa por múltiples transiciones jurídicamente relevantes (`TramiteRegistrado` → `PagoAsociado` → `EmisionSolicitada` → `TramiteEmitido`, o bien `EmisionConError` → `PagoAnulacionSolicitada` → `TramiteRechazado`). Para efectos de auditoría diplomática y trazabilidad frente al ciudadano:
- No basta con conocer únicamente el último estado sobrescrito en una fila mutable (`UPDATE tramites SET estado = ...`), pues se perdería el registro exacto de cuándo ocurrió cada transición y qué datos tenía cada hecho.
- Al mismo tiempo, las consultas ciudadanas (`GET /api/tramites/{id}` y `GET /api/tramites?dni=`) y la búsqueda por metadatos del expediente requieren lecturas rápidas sobre una estructura plana sin reconstruir todos los eventos en cada lectura.

---

## 2. Decisión

Adoptamos **Event Sourcing** combinado con **CQRS (Command Query Responsibility Segregation)** dentro de `tramites-service`:

1. **Modelo de Escritura (*Event Store* append-only — `tramites.eventos_tramite`):**
   - Cada cambio de estado del agregado `Tramite` produce uno o más hechos inmutables (`EventoDominioTramite`) que se insertan en `tramites.eventos_tramite(id, tramite_id, version, tipo, datos jsonb, ocurrido_en)`.
   - La tabla admite **solo inserciones (`INSERT`)**; nunca se ejecutan `UPDATE` ni `DELETE`.
   - La concurrencia entre hilos se controla mediante **bloqueo optimista** a través de la restricción `UNIQUE (tramite_id, version)`.
   - El estado del agregado se reconstruye de forma determinista con `Tramite.reconstruirDesdeEventosVersionados(historial)` invocando `aplicar(evento)`.
2. **Modelo de Lectura CQRS (`tramites.vista_tramite` y `ProyectorVistaTramite`):**
   - `ProyectorVistaTramite` proyecta síncronamente el estado consolidado del agregado hacia la tabla desnormalizada `tramites.vista_tramite` e invalida el puerto de caché (`CacheConsultaTramitePort`).
   - Todas las consultas de estado (`GET /api/tramites/{id}`, `GET /api/tramites?dni=` y gRPC `ConsultarTramite`) leen **exclusivamente** de `tramites.vista_tramite`.
   - El endpoint administrativo `POST /admin/proyecciones/vista-tramite/reconstruir` permite borrar `vista_tramite` y regenerarla íntegramente reproduciendo todos los flujos de `eventos_tramite`.

---

## 3. Alternativas Consideradas

| Alternativa | Ventajas | Motivo de descarte |
| --- | --- | --- |
| **1. Event Sourcing (`eventos_tramite`) + Proyección CQRS síncrona (`vista_tramite`) (Elegida)** | Bitácora de auditoría inmutable nativa (`GET /api/tramites/{id}/historial`), capacidad de reconstruir vistas de lectura desde cero y lecturas rápidas O(1) sobre `vista_tramite`. | Requiere mantener el proyector y versionar cuidadosamente el esquema JSON de los eventos de dominio. |
| **2. Modelo CRUD tradicional con sobrescritura de fila (`UPDATE`)** | Menor cantidad de clases iniciales. | Destruye la historia de transiciones al sobrescribir columnas; si se agrega una tabla de auditoría manual por triggers, esta suele desincronizarse de la lógica del dominio y no permite reconstruir el agregado. |
| **3. Event Sourcing sin proyección CQRS (reconstruir en cada `GET`)** | No requiere tabla `vista_tramite`. | Impide realizar búsquedas eficientes por columnas secundarias (`WHERE dni = ?` o paginación ordenada por `actualizado_en`) sobre 1 000 000 de trámites. |

---

## 4. Consecuencias y Evidencia

- **Positivas:**
  - El ciudadano y el funcionario pueden consultar el historial completo de eventos en `GET /api/tramites/{id}/historial`.
  - Si se requiere agregar una nueva columna o índice a la vista de lectura (como en el laboratorio de optimización de la Sesión 5), `vista_tramite` puede truncarse y reconstruirse en segundos desde `eventos_tramite`.
- **Negativas / Riesgos aceptados:**
  - Mayor complejidad de modelado en comparación con CRUD simple, justificada en el agregado central `Tramite` por sus requisitos de auditoría y coordinación con la Saga.
- **Evidencia en el código:**
  - Dominio: `Tramite.java`, `EventoDominioTramite.java`, `TramiteEventSourcingTest.java`.
  - Proyección CQRS: `ProyectorVistaTramite.java`, `ProyeccionAdminController.java`.
  - Persistencia: `V2__event_sourcing_cqrs_outbox.sql`, `EventoTramiteStoreAdapter.java`, `VistaTramiteJpaAdapter.java`.

