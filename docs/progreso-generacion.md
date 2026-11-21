# Registro de Progreso de Generación — `mrree-arquitectura-software`

Registro automatizado del orquestador de generación por prompt.

| Archivo de Prompt | Estado | Commit | Fecha | Observaciones |
| --- | --- | --- | --- | --- |
| `prompts/09-1-m3-base-y-expediente.md` | completado | `7fc6b3a` | 2026-11-18 | Base autocontenida de `m3-datos/`, esquemas aislados (*Database-per-Service*) y `tramites.expediente` JSONB + índice GIN. |
| `prompts/09-2-m3-event-sourcing-cqrs-outbox.md` | completado | `2d7b111` | 2026-11-19 | Event Sourcing (`eventos_tramite`), CQRS (`ProyectorVistaTramite`, `vista_tramite` con `motivo_rechazo` y sin índice por `dni`) y Outbox transaccional (`RelayOutbox`). Nota: `contrato-global.md` 6.3 menciona `motivo_rechazo` en `solucion/a3`, pero los prompts `09-2`, `09-4` y `09-6` indican incluir la columna en la vista desde `09-2` como andamiaje para que `git diff main...solucion/a3` solo toque `SagaSolicitudTramite.java`, `SagaCompensacionTest.java`, `A3.md` y `A3-solucion.md`. |
| `prompts/09-3-m3-pagos-emision.md` | completado | `f8d1875` | 2026-11-19 | Pagos idempotentes con `Idempotency-Key` y Outbox propio (`RegistrarPagoService`, `AnularPagoService`), `emision-service` (`:8087`), consumidores en `notificaciones-service` y script `pagar-dos-veces.ps1`/`.sh` verificados. |
| `prompts/09-4-m3-saga.md` | completado | `989bd96` | 2026-11-19 | Saga orquestada `SagaSolicitudTramite`, tabla `tramites.saga_instancia`, andamiaje A3 (`AnulacionPagoPort`, `alFallarEmision` con TODO y `SagaCompensacionTest` `@Disabled`), frontend M3 y colección Postman `m3.postman_collection.json`. |
| `prompts/09-5-m3-optimizacion-y-cache.md` | completado | `2fb15bc` | 2026-11-19 | Carga masiva (1 000 000 filas en ~18.77 s), `consultas-optimizacion.sql` (`Seq Scan` 119.8 ms vs `Index Scan` 0.114 ms; `OFFSET 900000` 734.9 ms vs *Keyset* 0.154 ms), matriz `m3-optimizacion.md`, andamiaje A4 (`CacheConsultaTramitePort`, `CacheNulaConsultaTramiteAdapter`, `X-Cache: MISS`, `RedisCacheConsultaTramiteAdapterTest` `@Disabled`) y `m3-datos/README.md`. |
| `prompts/09-6-m3-verificacion-y-a3.md` | completado | `89b8345` (`main`, tag `s4`) / `489bfe5` (`solucion/a3`) | 2026-11-19 | Verificación integral de `m3-datos` (17 rutas de presentaciones verificadas), guía `docs/asignaciones/A3.md`, tag `s4` en `main` y solución de referencia en `solucion/a3` (`SagaCompensacionTest` en verde, compensación E2E verificada con `EMISION_SIMULAR_FALLA=true` y diff de exactamente 4 archivos respecto a `main`). |
| `prompts/10-1-m4-base-y-opentelemetry.md` | completado | `14acbff` | 2026-11-19 | Copia autosuficiente de `m4-observabilidad`, agente Java OpenTelemetry (`2.10.0`), `grafana/otel-lgtm:0.8.1`, logs JSON con `trace_id`/`span_id`, propagación de `traceparent` por RabbitMQ y Outbox, y memoria total ~3.17 GB (≤ 6 GB). |
| `prompts/10-2-m4-metricas-tableros-slo.md` | completado | `e3de20e` | 2026-11-19 | Métricas de negocio Micrometer (`consular_tramites_registrados_total`, `consular_pagos_confirmados_total`, `consular_emision_duracion_seconds`), `@WithSpan` con `saga.paso` en `SagaSolicitudTramite`, latencia inyectada (`LATENCIA_INYECTADA_MS=3000` → 3386 ms), tableros aprovisionados en Grafana y `docs/observabilidad/slo.md`. |
| `prompts/10-3-m4-c4-y-a4.md` | completado | `48b49b5` (`main`, tag `s5`) / `9857ae6` (`solucion/a4`) | 2026-11-20 | Modelo C4 en Structurizr DSL (`docs/c4/workspace.dsl`) validado con `structurizr/cli:2025.11.09`, contenedor `m4-structurizr` (`:8090`), guía `docs/asignaciones/A4.md`, tag `s5` en `main` y solución de referencia en `solucion/a4` (`RedisCacheConsultaTramiteAdapter`, `X-Cache: MISS -> HIT -> MISS` y `git diff --stat main...solucion/a4` de 6 archivos exactos). |
| `prompts/10-4-m4-gobernanza-y-deuda.md` | completado | `ec8c9dc` (`main`) / `82d29d9` (`demo/strangler`) | 2026-11-20 | Estándares en `docs/estandares/` (`lineamientos-api.md` v0.1, `.spectral.yaml`, `estandar-observabilidad.md`, `reglas-arquitectura.md`, `checklist-revision-arquitectura.md`), `validar-apis.ps1`/`.sh` con Spectral, perfil `calidad` con SonarQube (`m4-sonarqube`, `:9000`) y `analizar-calidad.ps1`/`.sh` verificado, `ADR-002` a `ADR-005` completos y rama `demo/strangler` verificada. |
| `prompts/10-5-m4-verificacion-y-proyecto-final.md` | completado | `76af084` (`main`, tag `s6`) | 2026-11-20 | Verificación integral `/verificar-modulo m4-observabilidad` (15 contenedores `healthy`, memoria ~3.17–3.72 GB ≤ 6 GB, 16 rutas verificadas, Spectral 0 errores sobre `/v3/api-docs` en vivo), `m4-observabilidad/README.md` completo, guía `docs/asignaciones/proyecto-final.md` y etiqueta `s6`. |
| `prompts/11-docs-adr-c4-guias.md` | completado | `aec1f7d` | 2026-11-20 | Revisión de coherencia de ADR, C4 (`structurizr/cli validate` 0 errores, Structurizr Lite `:8090` con 4 vistas), estándares y guías existentes; creación de `docs/adr/README.md`, guías de instalación (`docs/instalacion/01..05`, `solucion-de-problemas.md`), `docs/glosario.md` y `README.md` raíz; validación offline de enlaces con `lycheeverse/lychee` (`172 Total, 0 Errors`). |
| `prompts/12-soluciones-asignaciones.md` | completado | `6085180` (`solucion/a1`), `9d08d66` (`solucion/a2`), `d0d93c6` (`solucion/a3`), `751ba65` (`solucion/a4`) | 2026-11-20 | Rebase limpio de las 4 ramas `solucion/a1` a `solucion/a4` sobre `main`; verificado `git diff --stat main...solucion/aN` limitado exclusivamente al alcance de cada asignación (4, 5, 4 y 6 archivos respectivamente) y pruebas `ReglaDeReprogramacionTest`, `TramiteObservadoConsumerTest`, `SagaCompensacionTest` y `RedisCacheConsultaTramiteAdapterTest` en verde. |
| `prompts/13-verificacion-final-y-publicacion.md` | completado | `ccb7912` | 2026-11-20 | Verificación integral del repositorio (andamiaje A1–A4 intacto en `main`, 4 pruebas con `@Disabled`, `lychee --offline` con 207 enlaces y 0 errores, tabla de tiempos y memoria en `docs/instalacion/README.md`), creación de `docs/trazabilidad-plan-codigo.md` (16 secciones 1.1 a 4.4 con evidencia ejecutable) y `docs/publicacion.md`, etiquetas `s1`…`s6` verificadas y sección "Publicación pendiente" documentada abajo. |

---

## Publicación pendiente

De acuerdo con las reglas de ejecución del orquestador (prohibido ejecutar `git push`, cambiar configuración de GitHub o crear releases), a continuación se listan los **comandos exactos, en orden**, para que usted publique el repositorio en GitHub, configure la protección de ramas y publique cada rama de solución cuando corresponda:

```powershell
# 1. Ubicarse en la raíz del repositorio en la rama main
cd c:\Jorge-Projects\mre_arquitectura\mrree-arquitectura-software
git checkout main

# 2. Crear el repositorio remoto en GitHub (reemplace <organizacion-o-docente> por su cuenta u organización)
gh repo create <organizacion-o-docente>/mrree-arquitectura-software --public --description "Plataforma Digital de Servicios Consulares y de Legalización — Curso Arquitectura de Software y Diseño de Soluciones (UTEC Posgrado – MRREE)"

# 3. Registrar o actualizar la URL del remoto origin
git remote add origin https://github.com/<organizacion-o-docente>/mrree-arquitectura-software.git
# (Si el remoto origin ya existía, use en su lugar:)
# git remote set-url origin https://github.com/<organizacion-o-docente>/mrree-arquitectura-software.git

# 4. Publicar la rama principal (main), la rama de demostración (demo/strangler) y las etiquetas de sesión (s1 a s6)
git push -u origin main
git push -u origin demo/strangler
git push origin --tags

# 5. Configurar la protección de la rama main en GitHub (bloqueando force-push y borrado)
gh api --method PUT "repos/<organizacion-o-docente>/mrree-arquitectura-software/branches/main/protection" `
  -H "Accept: application/vnd.github+json" `
  -F "required_linear_history=true" `
  -F "allow_force_pushes=false" `
  -F "allow_deletions=false" `
  -F "enforce_admins=false" `
  -f "required_pull_request_reviews=null" `
  -f "restrictions=null"

# 6. Publicar cada rama de solución ÚNICAMENTE después del cierre de entrega de cada asignación:
#    - Tras cierre de A1 (inicio de Sesión 3):
git push -u origin solucion/a1
#    - Tras cierre de A2 (inicio de Sesión 4):
git push -u origin solucion/a2
#    - Tras cierre de A3 (inicio de Sesión 5):
git push -u origin solucion/a3
#    - Tras cierre de A4 (inicio de Sesión 6):
git push -u origin solucion/a4
```

