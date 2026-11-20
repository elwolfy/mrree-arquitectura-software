# Proyecto Final Integrador — Evolución, Observabilidad y Gobernanza en la Plataforma Consular

- **Módulo de Trabajo:** `m4-observabilidad` (junto con `docs/adr/` y `docs/c4/workspace.dsl`)
- **Modalidad:** Individual o en pareja
- **Presentación:** Demostración en vivo de **5 minutos** durante la **Sesión 6**
- **Rama de Entrega:** `pf-<usuario>` en su **fork** del repositorio

---

## 1. Objetivo del Proyecto Final

Diseñar, implementar, probar, instrumentar y documentar una **nueva funcionalidad de negocio del dominio consular** dentro de la plataforma del **Módulo 4 (`m4-observabilidad`)**, demostrando el dominio práctico de los pilares del curso:
1. **Arquitectura Hexagonal y DDD** verificada con pruebas unitarias, de integración (Testcontainers) y de arquitectura (**ArchUnit**).
2. **Gobernanza de APIs REST** validada sin errores con **Spectral** (`.\scripts\validar-apis.ps1`).
3. **Observabilidad de Extremo a Extremo** con al menos **una métrica de negocio nueva** y **una traza distribuida con spans enriquecidos** visibles en **Grafana** (`http://localhost:3000`).
4. **Documentación Arquitectónica Viva**: un **ADR** nuevo en `docs/adr/` y la actualización correspondiente del **Modelo C4** en [docs/c4/workspace.dsl](../c4/workspace.dsl).

> **Importante:** Este documento es la guía de especificación y evaluación del Proyecto Final; la rama `main` **no** incluye una implementación resuelta de este proyecto.

---

## 2. Caso de Referencia Sugerido (o Propuesta Equivalente)

Puede implementar el siguiente caso de referencia o proponer una funcionalidad equivalente del dominio consular de complejidad similar:

### Caso de Referencia: Alerta y Gestión de Trámites Próximos a Vencer (`PENDIENTE_PAGO`)

En la operación consular real, las solicitudes de trámite registradas que permanecen en estado `PENDIENTE_PAGO` por más de un umbral configurable deben identificarse para alertar al ciudadano antes de que caduque su expediente:

1. **Regla de Dominio y Caso de Uso (`tramites-service`)**:
   - Exponer un endpoint en `kebab-case` (por ejemplo, `POST /api/tramites/alertas-vencimiento` o `GET /api/tramites/proximos-a-vencer?minutos=...`) que identifique en la proyección o bitácora los trámites en `PENDIENTE_PAGO` próximos a vencer.
   - Publicar un evento de dominio/integración (p. ej. `tramite.proximo.vencer` vía `tramites.outbox`) que `notificaciones-service` consuma de manera idempotente para enviar el aviso al ciudadano en Mailpit.
2. **Otras propuestas válidas del dominio consular**:
   - Verificación pública de autenticidad de documentos emitidos (`GET /api/documentos/{numeroDocumento}/verificacion`) entre `gateway`, `emision-service` y `tramites-service`.
   - Priorización de atención consular de urgencia humanitaria en `citas-service` y `tramites-service` con notificación prioritaria.
   - Revocación o rectificación de apostilla emitida con evento de auditoría en Event Sourcing y aviso al ciudadano.

---

## 3. Requisitos Técnicos Obligatorios

### 3.1 Arquitectura y Pruebas (`m4-observabilidad`)
- Respetar estrictamente la separación hexagonal (`domain`, `application.port.in`, `application.port.out`, `application.service`, `adapter.in`, `adapter.out`).
- Todas las pruebas de **ArchUnit** (`ReglasDeArquitecturaTest`) deben pasar sin modificaciones ni exclusiones.
- Incluir al menos **una prueba unitaria de dominio** y **una prueba de integración** (`@Tag("integracion")`) que validen el caso feliz y un caso de error (`ProblemDetail` RFC 9457).
- El contrato OpenAPI actualizado debe pasar `.\m4-observabilidad\scripts\validar-apis.ps1` con **0 errores** en Spectral.

### 3.2 Instrumentación y Observabilidad en Grafana
- **Métrica de negocio (Micrometer):** Registrar al menos una métrica nueva con prefijo `consular_` detrás de un puerto de salida hexagonal (tomando como modelo [MicrometerMetricasTramiteAdapter.java](../../m4-observabilidad/tramites-service/src/main/java/pe/utec/consular/tramites/adapter/out/observability/MicrometerMetricasTramiteAdapter.java)), por ejemplo: `consular_tramites_alertas_vencimiento_total` o un histograma `Timer`.
- **Traza distribuida y Span (`@WithSpan`):** Anotar el caso de uso o paso de coordinación con `@WithSpan` y atributos de negocio (`@SpanAttribute`), verificando en **Grafana Explore → Tempo** la traza completa y el salto a los logs JSON en **Loki** (**"Logs for this span"**).
- **Panel en Grafana:** Agregar un panel que consulte la nueva métrica en PromQL dentro de `m4-observabilidad/grafana/dashboards/flujo-tramite.json` (o `vision-general.json`).

### 3.3 Documentación (ADR + Modelo C4)
- **ADR nuevo:** Crear `docs/adr/ADR-007-<titulo-de-su-funcionalidad>.md` con la plantilla del curso (Contexto, Decisión, Alternativas consideradas, Consecuencias y Evidencia).
- **Modelo C4 actualizado:** Actualizar [docs/c4/workspace.dsl](../c4/workspace.dsl) (vista de componentes o vista dinámica) reflejando el nuevo componente o flujo, y verificar que valide sin errores con Structurizr CLI y se visualice en `http://localhost:8090` (`.\scripts\levantar-c4.ps1`).

---

## 4. Estructura de la Demostración en Vivo (5 Minutos — Sesión 6)

1. **Minuto 1 — Contexto, ADR y Modelo C4 (`http://localhost:8090`):**
   - Presentar qué problema de negocio consular resuelve la funcionalidad, la decisión principal en `ADR-007` y el componente/relación en Structurizr.
2. **Minutos 2 y 3 — Ejecución Funcional y Gobernanza:**
   - Invocar la funcionalidad a través del API Gateway (`http://localhost:8080`) o el frontend, mostrar la respuesta exitosa, un caso de error `ProblemDetail` y la validación limpia de Spectral (`.\scripts\validar-apis.ps1`).
3. **Minuto 4 — Evidencia en Grafana (`http://localhost:3000`):**
   - Mostrar el panel con la nueva métrica de negocio en el tablero de Grafana, abrir la traza en **Tempo** con sus atributos de span y saltar a los logs JSON en **Loki**.
4. **Minuto 5 — Pruebas y Cierre:**
   - Mostrar la ejecución en verde de las pruebas unitarias, de integración y ArchUnit.

---

## 5. Cómo Entregar

1. En su **fork** del repositorio, cree la rama `pf-<usuario>` (o `pf-<usuario1>-<usuario2>` si trabaja en pareja) a partir de `main`:
   ```bash
   git checkout -b pf-<usuario>
   ```
2. Verifique que se cumplen todos los puntos de [docs/estandares/checklist-revision-arquitectura.md](../estandares/checklist-revision-arquitectura.md) y que `mvn -B -f m4-observabilidad verify` pasa en verde.
3. Suba la rama `pf-<usuario>` a su fork antes del inicio de la **Sesión 6**.

---

## 6. Rúbrica de Evaluación

| Criterio | Peso | Descripción de lo evaluado |
| --- | --- | --- |
| **1. Funcionamiento de extremo a extremo** | **40 %** | La funcionalidad cumple las reglas de negocio propuestas, se ejecuta limpiamente en Docker Compose (`levantar.ps1`) a través del API Gateway (`:8080`), maneja errores con `ProblemDetail` (RFC 9457) y supera la validación OpenAPI de Spectral (`0 errores`). |
| **2. Arquitectura y pruebas automatizadas** | **30 %** | Respeta las capas y convenciones de la arquitectura hexagonal (`ReglasDeArquitecturaTest` en verde sin reglas deshabilitadas), incluye pruebas unitarias de dominio y pruebas de integración con Testcontainers en verde (`mvn -f m4-observabilidad verify`). |
| **3. Evidencia de observabilidad en Grafana** | **15 %** | Métrica de negocio registrada con Micrometer y visible con datos reales en un panel de Grafana; traza distribuida con span enriquecido (`@WithSpan`) visible en Tempo y correlacionada con los logs JSON en Loki. |
| **4. ADR y documentación C4** | **15 %** | `ADR-007` completo con análisis de alternativas y consecuencias reales; `docs/c4/workspace.dsl` actualizado, validado sin errores con Structurizr CLI y presentado durante la demostración de 5 minutos. |
