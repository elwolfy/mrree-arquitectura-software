# Objetivo de Nivel de Servicio (SLO) — Consulta de Estado de Trámite Consular

> **Módulo 4 (Observabilidad y Gobernanza Técnica)** — Referencia: sección 7 del [contrato-global.md](../especificacion/contrato-global.md).

---

## 1. Definición de SLI, SLO y Presupuesto de Error

En la Plataforma Digital de Servicios Consulares y de Legalización, la operación más frecuente ejecutada por los ciudadanos (y sondeada periódicamente desde la pantalla `/tramites/consulta` del frontend) es la consulta del estado actual de un trámite a partir de la proyección de lectura CQRS (`GET /api/tramites/{id}`).

| Concepto | Definición en la Plataforma Consular | Valor / Umbral |
| --- | --- | --- |
| **SLI (Service Level Indicator)** | Proporción de solicitudes HTTP `GET /api/tramites/{id}` atendidas con latencia inferior a **500 ms** (`le="0.5"`). | $\text{SLI} = \frac{\text{Solicitudes con latencia} \le 0.5\text{ s}}{\text{Total de solicitudes a GET /api/tramites/\{id\}}}$ |
| **SLO (Service Level Objective)** | Objetivo de cumplimiento del SLI en una ventana móvil. | **$\ge 99{,}0\%$** de consultas con latencia $< 500\text{ ms}$ |
| **Presupuesto de Error (Error Budget)** | Fracción máxima permitida de consultas lentas ($\ge 500\text{ ms}$) antes de congelar despliegues de nuevas funcionalidades y priorizar optimizaciones (p. ej., caché Redis de la Asignación A4 o índices en la proyección CQRS). | **$1{,}0\%$** ($100\% - 99{,}0\%$) |

### Cálculo práctico del Presupuesto de Error (Ventana de 30 días)

- Si el servicio recibe **1 000 000 de consultas** `GET /api/tramites/{id}` en un mes:
  - **Consultas que deben cumplir $< 500\text{ ms}$ (99 %):** al menos **990 000 consultas**.
  - **Presupuesto de error mensual (1 %):** hasta **10 000 consultas** pueden superar los $500\text{ ms}$ sin incumplir el SLO.
- En términos de tiempo equivalente continuo en un mes de 30 días ($43\,200\text{ minutos}$), un presupuesto de error del $1\%$ equivale a **$432\text{ minutos}$ ($7{,}2\text{ horas}$)** de degradación por encima del umbral.

---

## 2. Instrumentación en `tramites-service`

Para que Prometheus pueda evaluar con exactitud la cubeta de $500\text{ ms}$ (`le="0.5"`), el microservicio `tramites-service` configura en `application.yml` la publicación del histograma de percentiles y los límites SLO de Micrometer sobre `http.server.requests`:

```yaml
management:
  metrics:
    tags:
      application: ${spring.application.name}
    distribution:
      percentiles-histogram:
        http.server.requests: true
      slo:
        http.server.requests: 50ms,100ms,250ms,500ms,1s,2s,5s
```

---

## 3. Consulta PromQL del SLI

### 3.1 Proporción del SLI en porcentaje (Ventana de 5 minutos)

Esta es la consulta exacta utilizada en el panel **"SLI / SLO: Consultas GET /api/tramites/{id} < 500 ms (Objetivo 99%)"** del tablero aprovisionado **"Plataforma consular – visión general"** (`m4-observabilidad/grafana/dashboards/vision-general.json`):

```promql
100 * (
  sum(rate(http_server_requests_seconds_bucket{application="tramites-service",method="GET",uri="/api/tramites/{id}",le="0.5"}[5m]))
  /
  clamp_min(sum(rate(http_server_requests_seconds_count{application="tramites-service",method="GET",uri="/api/tramites/{id}"}[5m])), 0.0001)
)
```

### 3.2 Tasa de consumo del Presupuesto de Error (Burn Rate)

Para alertar cuando el presupuesto de error del $1\%$ ($0{,}01$) se consume a un ritmo superior al sostenible:

```promql
(
  1 - (
    sum(rate(http_server_requests_seconds_bucket{application="tramites-service",method="GET",uri="/api/tramites/{id}",le="0.5"}[5m]))
    /
    clamp_min(sum(rate(http_server_requests_seconds_count{application="tramites-service",method="GET",uri="/api/tramites/{id}"}[5m])), 0.0001)
  )
) / 0.01
```

- Un **Burn Rate = 1** indica que el servicio consume exactamente el $100\%$ de su presupuesto de error al final de la ventana.
- Un **Burn Rate > 1** (por ejemplo, $> 14{,}4$ en una ventana de 1 hora) dispara una alerta crítica de agotamiento acelerado del presupuesto de error.

---

## 4. Visualización en Grafana

Al levantar `m4-observabilidad` (`.\scripts\levantar.ps1`) e ingresar a Grafana (`http://localhost:3000`, `admin` / `admin`):
1. Abrir **Dashboards → Plataforma Consular → Plataforma consular – visión general**.
2. El primer panel (**Gauge** en la esquina superior izquierda) muestra el **SLI en porcentaje** con tres umbrales de color:
   - **Verde ($\ge 99{,}0\%$):** dentro del SLO.
   - **Ámbar ($98{,}0\% - 98{,}99\%$):** presupuesto de error en riesgo.
   - **Rojo ($< 98{,}0\%$):** violación del SLO.

