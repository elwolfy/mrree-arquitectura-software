# Matriz Comparativa S1: Estilos Arquitectónicos (`m1-estilos`)

> **Sesión:** S1 — Sección 1.2 del sílabo (*Monolito Modular vs. Microservicios vs. Serverless / FaaS*).
> **Variantes evaluadas en `m1-estilos`:**
> - **Monolito Modular:** `.\scripts\levantar.ps1 monolito` (`m1-monolito`)
> - **Microservicios:** `.\scripts\levantar.ps1 microservicios` (`m1-citas` + `m1-notificaciones`)
> - **Serverless (FaaS emulado):** `.\scripts\levantar.ps1 serverless` (`m1-fn-consultas`, `m1-fn-reservar`, `m1-fn-notificar`, `m1-enrutador`)

Todas las variantes exponen el mismo contrato HTTP en `http://localhost:8081` y son consumidas por el mismo visor Angular (`http://localhost:4200`) y el mismo script de carga k6 (`docs/pruebas/k6/citas.js`).

---

## 1. Matriz Cualitativa de Criterios de Comparación

| Criterio | Monolito Modular (`monolito`) | Microservicios (`microservicios`) | Serverless / FaaS (`serverless`) |
| --- | --- | --- | --- |
| **Despliegue** | Unidad única de despliegue (1 artefacto JAR / 1 contenedor de aplicación). Un cambio en cualquier módulo exige redesplegar todo el artefacto. | Unidades de despliegue independientes por contexto acotado (`citas-service` y `notificaciones-service`), cada uno con su ciclo de release y esquema propio. | Unidad funcional por operación (`listarOficinas`, `reservarCita`, `notificar`, etc.) detrás de un API Gateway / enrutador. |
| **Escalabilidad** | Escalado horizontal grueso: se clona todo el proceso monolítico aunque el cuello de botella esté solo en consultas de disponibilidad. | Escalado selectivo por servicio: se puede escalar `citas-service` de forma independiente a `notificaciones-service`. | Escalado elástico por función e invocación (en nube pública escala automáticamente de 0 a $N$ instancias según la tasa de eventos). |
| **Aislamiento de fallas** | Bajo a nivel de proceso: una falla síncrona no controlada o agotamiento de memoria/hilos afecta a todos los módulos del proceso. | Alto: la caída de `m1-notificaciones` es contenida mediante *Circuit Breaker* y *timeout* (Resilience4j), manteniendo disponible `m1-citas`. | Alto: cada función se ejecuta de forma aislada y sin estado; `reservarCita` invoca a `notificar` de manera asíncrona con presupuesto de 300 ms. |
| **Complejidad operativa** | Baja: un solo proceso que monitorear, depurar y desplegar; comunicación en memoria sin latencia de red interna. | Media-Alta: requiere gestión de múltiples servicios, descubrimiento/red, timeouts, resiliencia, trazabilidad distribuida y migraciones por esquema. | Media en infraestructura (gestionada por el proveedor cloud), pero alta en configuración de enrutamiento, *cold starts*, límites de conexiones a BD y observabilidad. |
| **Costo** | Eficiente y predecible para cargas estables o moderadas; menor huella base de memoria RAM (una sola JVM). | Mayor costo base fijo: múltiples JVMs activas permanentemente y sobrecosto de serialización y red entre contenedores. | Pago por uso real (invocaciones y GB-segundo) ideal para cargas variables o esporádicas; en emulación local con JVMs fijas consume más RAM por contenedor. |
| **Consistencia** | Transacciones ACID locales (`@Transactional`) inmediatas entre módulos sobre la misma base de datos. | Consistencia eventual o coordinación explícita entre servicios; cada servicio es dueño exclusivo de su esquema (`citas` vs. `notificaciones`). | Sin estado entre invocaciones (*stateless*); consistencia eventual entre funciones desacopladas (`reservarCita` confirma antes o independientemente de `notificar`). |

---

## 2. Matriz de Medición Experimental (Para Completar en Clase — S1)

Ejecute cada variante una por una desde `m1-estilos/` y registre los valores observados en su equipo:

```powershell
.\scripts\levantar.ps1 <monolito|microservicios|serverless>
.\scripts\medir.ps1
.\scripts\carga.ps1 citas
```

| Métrica / Experimento | Cómo obtener el valor | Esperado (Referencia / Contrato 4.3) | Monolito (`monolito`) | Microservicios (`microservicios`) | Serverless (`serverless`) |
| --- | --- | --- | --- | --- | --- |
| **Tiempo de arranque** | Ejecutar `.\scripts\medir.ps1` y leer la sección `1. Tiempos de Arranque Registrados en Logs` (`Started ... in N seconds` o `Función lista en N ms`). | ~10–20 s por proceso JVM en contenedor local. | *(completar)* | *(completar)* | *(completar)* |
| **Memoria total** | Ejecutar `.\scripts\medir.ps1` y leer `Memoria total consumida (aprox.)` al pie de la tabla `docker stats`. | Monolito < Microservicios < Serverless (por cantidad de JVMs activas en local). | *(completar)* | *(completar)* | *(completar)* |
| **Número de contenedores** | Ejecutar `.\scripts\medir.ps1` y leer `Número de contenedores activos (m1-*)`. | Monolito: `4` \| Microservicios: `5` \| Serverless: `7`. | *(completar)* | *(completar)* | *(completar)* |
| **p95 (latencia en ms)** | Ejecutar `.\scripts\carga.ps1 citas` y leer `Latencia p95` en el resumen final de k6. | Umbral informativo: `< 2000 ms` (`2 s`). | *(completar)* | *(completar)* | *(completar)* |
| **Solicitudes por segundo** | Ejecutar `.\scripts\carga.ps1 citas` y leer `Solicitudes por segundo (req/s)` en el resumen final de k6. | Depende de CPU/RAM del equipo (referencia: 80–180 req/s con 50 VUs y `sleep(0.2)`). | *(completar)* | *(completar)* | *(completar)* |
| **Comportamiento de la reserva ante falla de notificaciones** | **Monolito:** activar `NOTIFICACIONES_SIMULAR_FALLA=true` en `.env`, levantar y reservar.<br>**Microservicios:** ejecutar `docker stop m1-notificaciones` y reservar.<br>**Serverless:** ejecutar `docker stop m1-fn-notificar` y reservar. | **Monolito:** revierte la transacción de reserva y responde **`500`**.<br>**Microservicios:** confirma la reserva (**`201`**) en `< 3 s` con `notificacion: PENDIENTE`.<br>**Serverless:** confirma la reserva (**`201`**) en `~300 ms` con `notificacion: PENDIENTE`. | *(completar)* | *(completar)* | *(completar)* |

