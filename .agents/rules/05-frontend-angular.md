---
description: Convenciones del frontend Angular + PrimeNG
activation: glob
glob: "frontend/**"
---

# Frontend Angular

## Propósito

Aplicación web ciudadana que recorre el flujo de negocio y hace visibles los conceptos de arquitectura (consistencia eventual, origen de los datos, variante en ejecución). **No es objeto de las asignaciones**: debe ser simple, estable y fácil de leer.

## Stack

- Angular (última versión estable), componentes standalone, rutas con carga diferida, signals para estado local, `HttpClient` con interceptores.
- PrimeNG + PrimeIcons (tema claro, sobrio, institucional neutro; sin logotipos ni marca del MRREE ni de UTEC).
- Sin librerías de estado global (NgRx no). Sin SSR.
- Idioma: español (es-PE) para textos, fechas y moneda (`PEN`).

## Configuración en tiempo de ejecución

Una sola imagen sirve a todos los módulos. La configuración se lee al iniciar desde `/assets/config.json`, generado por nginx a partir de variables de entorno (`envsubst` en el entrypoint):

```json
{
  "apiBaseUrl": "http://localhost:8081",
  "modulo": "m1",
  "funcionalidades": { "citas": true, "tramites": false, "pagos": false, "bffInicio": false, "historial": false }
}
```

| Módulo | `apiBaseUrl` | Funcionalidades activas |
| --- | --- | --- |
| m1 | `http://localhost:8081` | citas |
| m2 | `http://localhost:8080` (gateway) | citas, tramites (sin pago), bffInicio |
| m3, m4 | `http://localhost:8080` | todas |

## Pantallas

| Ruta | Pantalla | Módulos |
| --- | --- | --- |
| `/` | Inicio: tarjeta "Sistema en ejecución" con `GET /api/sistema/info` (m1) y accesos | todos |
| `/citas/nueva` | Reservar cita: oficina → fecha → cupo libre → datos → confirmación | todos |
| `/citas/consulta` | Consultar citas por DNI o código; cancelar | todos |
| `/tramites/nuevo` | Iniciar trámite vía `POST /bff/tramites` (valida DNI con la PIDE simulada) | m2+ |
| `/tramites/:id/pago` | Pago simulado con `Idempotency-Key` generado en el cliente (UUID) | m3+ |
| `/tramites/consulta` | Estado del trámite con sondeo cada 2 s durante 30 s (muestra consistencia eventual) e historial de eventos | m2+ (historial m3+) |
| `/mi-espacio` | Vista agregada del BFF (`GET /bff/inicio?dni=`) | m2+ |

## Elementos didácticos obligatorios

- En disponibilidad: etiqueta visible del `origen` (`Base de datos` / `Caché – actualizado hace N s`) para la prueba CAP de S2.
- En la reserva: mostrar el estado `notificacion` devuelto (`ENVIADA` / `PENDIENTE`) para la comparación de estilos de S1.
- En la consulta de trámite: línea de tiempo de estados y aviso "El estado se actualiza de forma asíncrona" mientras sondea.
- Errores: mostrar `title` y `detail` del `ProblemDetail` en un `p-message`, nunca un error genérico.
- Pie de página con `modulo` y `apiBaseUrl` actuales.

## Estructura

```text
frontend/src/app/
├── core/        config, interceptores (errores, idempotencia), modelos
├── citas/       componentes y servicios de citas
├── tramites/    componentes y servicios de trámites y pagos
├── inicio/      inicio y mi-espacio
└── shared/      componentes reutilizables
```

## Imagen Docker

Multi-etapa: `node:22-alpine` (npm ci + build) → `nginx:<estable>-alpine`. `nginx.conf` con `try_files` para rutas de Angular y entrypoint que genera `config.json`. Puerto interno 80, publicado en 4200.
