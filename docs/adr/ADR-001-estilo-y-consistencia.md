# ADR-001: Estilo arquitectónico inicial de la plataforma y consistencia de las operaciones de citas

- **Estado:** Aceptado (Sección 1) / En revisión para Asignación A1 (Sección 2)
- **Fecha:** 2026-11-17
- **Módulo / Contexto:** M1 – Estilos de Arquitectura (`m1-estilos`) / Gestión de Citas Consulares

---

## Parte 1: Estilo de la plataforma (Ejemplo resuelto)

### 1.1 Contexto

La Plataforma Digital de Servicios Consulares y de Legalización inicia su construcción con un equipo reducido, plazos acotados para entrar en operación en seis consulados piloto (`MAD`, `BUE`, `SCL`, `MIA`, `MIL`, `TYO`) y un presupuesto de infraestructura restringido en estaciones de desarrollo y entornos de prueba ($\le 6\text{ GB}$ de RAM).

 Al mismo tiempo, el dominio presenta fronteras funcionales bien diferenciadas (`citas`, `notificaciones`, `tramites`) que en etapas posteriores del proyecto (M2–M4) evolucionarán hacia servicios distribuidos con integración externa (RENIEC/PIDE, pasarela de pagos y emisión documental). Necesitamos elegir el estilo arquitectónico de partida comparando tres candidatos evaluados experimentalmente en [m1-estilos.md](../matrices/m1-estilos.md) bajo los escenarios de calidad de [m1-escenarios-calidad.md](../matrices/m1-escenarios-calidad.md).

### 1.2 Decisión

> **Adoptamos un Monolito Modular Evolutivo con arquitectura hexagonal interna (`m1-estilos/monolito-modular`) como estilo de arranque de la plataforma consular**, imponiendo fronteras estrictas entre módulos mediante Spring Modulith y pruebas automatizadas de arquitectura con ArchUnit (`ReglasDeArquitecturaTest`), de modo que cada módulo pueda extraerse a microservicios en fases posteriores sin reescribir su dominio.

### 1.3 Alternativas Consideradas

| Alternativa | Ventajas observadas en M1 | Motivo de descarte para la fase inicial |
| --- | --- | --- |
| **1. Monolito Modular Evolutivo (Elegido)** | Menor consumo de memoria (`~363 MiB` en 4 contenedores), menor latencia `p95` (`69.73 ms` a 50 VUs), transacciones ACID locales simples y fronteras verificadas por `ModulosTest` y `ReglasDeArquitecturaTest`. | Requiere disciplina para desacoplar efectos secundarios (como el envío de notificaciones) mediante eventos internos o colas cuando se busca aislar fallas. |
| **2. Microservicios desde el día 1** | Aislamiento nativo de fallas con Circuit Breaker (`notificacion: PENDIENTE` cuando cae `m1-notificaciones`) y escalamiento independiente por proceso. | Duplica la huella de memoria (`~639 MiB`), duplica la latencia `p95` (`136.40 ms`) e introduce sobrecarga operativa prematura (múltiples despliegues, trazabilidad distribuida y consistencia eventual) antes de estabilizar el dominio. |
| **3. Serverless (FaaS + Enrutador)** | Escalamiento por función (`fn-consultas`, `fn-reservar`, `fn-notificar`) y desacoplamiento asíncrono de notificaciones. | Mayor huella agregada en local (`~897 MiB` en 7 contenedores), mayor latencia `p95` (`182.11 ms`), penalización por arranque en frío (*cold start*) y fricción para gestionar pools de conexiones transaccionales hacia PostgreSQL. |

### 1.4 Consecuencias

- **Positivas:**
  - Un único artefacto desplegable con tiempos de compilación y diagnóstico reducidos.
  - Los paquetes `citas`, `notificaciones` y `tramites` mantienen su dominio libre de dependencias de Spring/JPA (verificado por `ReglasDeArquitecturaTest`), facilitando la extracción futura en M2.
- **Negativas / Riesgos aceptados:**
  - La invocación síncrona de notificaciones dentro de la misma transacción acopla la disponibilidad de la reserva al canal de notificación (como se demuestra con `NOTIFICACIONES_SIMULAR_FALLA=true` devolviendo `500`). Mitigación en evolución (M2): publicar eventos hacia un broker o usar el patrón Outbox.

### 1.5 Evidencia

- Matriz comparativa y mediciones reales: [docs/matrices/m1-estilos.md](../matrices/m1-estilos.md).
- Escenarios de atributos de calidad: [docs/matrices/m1-escenarios-calidad.md](../matrices/m1-escenarios-calidad.md).
- Verificación estructural: `ModulosTest.java` y `ReglasDeArquitecturaTest.java` en `m1-estilos/monolito-modular`.

---

## Parte 2: Consistencia de la reprogramación (Asignación A1 – Tarea 1)

<!--
  GUÍA PARA EL PARTICIPANTE (Asignación A1 – Tarea 1):
  Complete las subsecciones siguientes para justificar la decisión CAP/PACELC de la operación
  PUT /api/citas/{citaId}/reprogramacion.
  Preguntas guía:
  1. ¿Qué ocurre en la base de datos cuando un ciudadano reprograma su cita (qué pasa con el cupo anterior y qué pasa con el nuevo cupo)?
  2. ¿Qué riesgo de negocio ocurriría si dos ciudadanos reprograman simultáneamente hacia el último cupo libre durante una partición o sin bloqueo?
  3. Revise ReprogramarCitaService.java: ¿qué método de CupoRepositoryPort se invoca para el cupo anterior y para el nuevo cupo?
  4. ¿Por qué se descarta una alternativa AP para esta operación y qué costo asumimos en términos de disponibilidad cuando la base de datos no responde?
-->

### 2.1 Decisión (CP o AP)

<!-- Indique explícitamente si la operación "Reprogramar cita" debe ser CP o AP y resuma la razón de negocio en un párrafo. -->

_Pendiente de completar en la Asignación A1._

### 2.2 Evidencia en el código y en las pruebas

<!-- Mencione la clase y el método exacto de bloqueo en m1-estilos/monolito-modular que respalda esta decisión, así como el código HTTP esperado ante una partición de red. -->

_Pendiente de completar en la Asignación A1._

### 2.3 Alternativa descartada

<!-- Describa en qué consistiría la alternativa opuesta (AP o CP) para la reprogramación y por qué fue descartada en el contexto consular. -->

_Pendiente de completar en la Asignación A1._

### 2.4 Consecuencias

<!-- Liste al menos una consecuencia positiva (integridad/negocio) y una consecuencia negativa aceptada (disponibilidad/experiencia en falla). -->

_Pendiente de completar en la Asignación A1._

