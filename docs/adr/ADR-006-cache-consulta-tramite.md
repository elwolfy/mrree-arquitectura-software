# ADR-006: Estrategia de caché de lectura para la consulta de estado de trámites (`GET /api/tramites/{id}`)

- **Estado:** En revisión para Asignación A4
- **Fecha:** 2026-11-19
- **Módulo / Contexto:** M3 – Gestión de Datos (`m3-datos`) y M4 – Modelo C4 (`docs/c4/workspace.dsl`) / Consulta de Trámites Consulares

---

## 1. Contexto

En la Plataforma Digital de Servicios Consulares y de Legalización, una vez que el ciudadano inicia y paga su trámite, la pantalla de consulta (`/tramites/consulta`) realiza un sondeo periódico (`GET /api/tramites/{id}` cada 2 segundos durante 30 segundos) para reflejar de manera asíncrona las transiciones de estado producidas por la Saga (`PENDIENTE_PAGO` → `EN_EMISION` → `EMITIDO`). Adicionalmente, el SLO definido en [slo.md](../observabilidad/slo.md) exige que al menos el **99 %** de las consultas `GET /api/tramites/{id}` respondan en menos de **500 ms**.

Aunque en `m3-datos` las lecturas ya consultan la proyección desnormalizada CQRS (`tramites.vista_tramite`), cada petición `GET /api/tramites/{id}` sin caché ejecuta una consulta SQL contra PostgreSQL. Necesitamos definir e implementar una estrategia de caché distribuida con **Redis** (`:6379`) detrás del puerto secundario `CacheConsultaTramitePort`.

<!--
  GUÍA PARA EL PARTICIPANTE (Asignación A4 – Tarea 1 Conceptual):
  Complete las secciones 2, 3, 4 y 5 respondiendo con fundamento arquitectónico:
  1. ¿Por qué elegimos el patrón Cache-Aside (Lazy Loading) con clave `tramite:<id>` y un TTL de 60 segundos?
  2. ¿Por qué no basta con esperar a que expire el TTL de 60 s y es indispensable que `ProyectorVistaTramite.proyectar(...)` invalide explícitamente la clave en Redis (`cacheConsultaTramitePort.invalidar(tramite.getId())`) cada vez que la Saga cambia el estado del trámite?
  3. ¿Qué alternativa se descartó (por ejemplo: TTL largo sin invalidación por eventos, o Write-Through sin TTL) y qué problema habría causado durante el sondeo del ciudadano?
  4. ¿Qué ocurre si Redis presenta una caída transitoria o qué ventana de obsolescencia máxima existe?
-->

---

## 2. Decisión

<!-- Describa el patrón elegido (Cache-Aside en Redis), el formato de clave (`tramite:<id>`), el TTL de 60 segundos, la cabecera diagnóstica `X-Cache: HIT|MISS` y la política de invalidación activa desde `ProyectorVistaTramite`. -->

_Pendiente de completar en la Asignación A4._

---

## 3. Justificación del TTL de 60 s Combinado con Invalidación al Cambiar el Estado

<!-- Explique por qué ambos mecanismos (TTL de 60 s + invalidación explícita en ProyectorVistaTramite) se complementan para evitar lecturas obsoletas (stale reads) cuando el trámite pasa de PENDIENTE_PAGO a EN_EMISION y EMITIDO. -->

_Pendiente de completar en la Asignación A4._

---

## 4. Alternativas Consideradas y Descartadas

<!-- Compare al menos dos alternativas (p. ej., solo TTL sin invalidación explícita vs. caché local en memoria JVM con Caffeine cuando hay múltiples réplicas de tramites-service) y explique por qué se descartaron. -->

_Pendiente de completar en la Asignación A4._

---

## 5. Consecuencias y Evidencia

<!-- Liste las consecuencias positivas (reducción de carga sobre PostgreSQL, cumplimiento del SLO < 500 ms, cabecera X-Cache: HIT/MISS) y negativas/riesgos aceptados, junto con los archivos del repositorio que implementan la decisión. -->

_Pendiente de completar en la Asignación A4._

