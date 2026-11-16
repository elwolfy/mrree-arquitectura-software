# Frontend Ciudadano — Angular + PrimeNG (`consular-frontend`)

## Propósito

Aplicación web ciudadana compartida por los cuatro módulos del curso (**M1–M4**). Hace visibles en pantalla los conceptos arquitectónicos clave del laboratorio:

- **Estilos arquitectónicos y acoplamiento (S1):** Tarjeta *"Sistema en ejecución"* (`GET /api/sistema/info`) y estado de la notificación (`ENVIADA` / `PENDIENTE`) al reservar una cita.
- **Teorema CAP y degradación elegante (S2):** Etiqueta de origen de datos en disponibilidad (`Base de datos` vs. `Caché – actualizado hace N s` cuando el monolito opera en modo `AP` con la red de datos desconectada).
- **Patrones de borde y BFF (S3 / M2):** Inicio de trámites vía fachada `POST /bff/tramites` y vista agregada `GET /bff/inicio?dni=` con avisos de degradación parcial.
- **Consistencia eventual, Sagas, Idempotencia y Event Sourcing (S4–S5 / M3):** Pago en línea con `Idempotency-Key` generado en el cliente y consulta de trámite con sondeo automático cada 2 s durante 30 s e historial de eventos.
- **Errores HTTP estandarizados (RFC 9457):** Visualización del `title` y `detail` del `ProblemDetail` en un banner `p-message`.

> **Uso en clase:** En las sesiones de laboratorio se utiliza **únicamente la imagen Docker** (`consular-frontend`), sin requerir que el participante instale Node.js ni Angular CLI en su máquina.

---

## Configuración en Tiempo de Ejecución (`/assets/config.json`)

Una sola imagen Docker sirve a todos los módulos. Al arrancar el contenedor Nginx, el script [`docker-entrypoint.d/40-config.sh`](./docker-entrypoint.d/40-config.sh) genera `/usr/share/nginx/html/assets/config.json` mediante `envsubst` a partir de variables de entorno:

| Variable de entorno | Valor por defecto | Descripción |
| --- | --- | --- |
| `API_BASE_URL` | `http://localhost:8081` | URL base del punto de entrada HTTP (M1 en `:8081` o API Gateway M2–M4 en `:8080`) |
| `MODULO` | `m1` | Identificador del módulo activo (`m1`, `m2`, `m3`, `m4`) |
| `FUNC_CITAS` | `true` | Habilita las pantallas de reserva y consulta de citas |
| `FUNC_TRAMITES` | `false` | Habilita el inicio y consulta de trámites (`true` en M2–M4) |
| `FUNC_PAGOS` | `false` | Habilita la pantalla de pago con `Idempotency-Key` (`true` en M3–M4) |
| `FUNC_BFF` | `false` | Habilita la vista agregada *Mi Espacio* (`true` en M2–M4) |
| `FUNC_HISTORIAL` | `false` | Habilita la visualización del historial de eventos Event Sourcing (`true` en M3–M4) |

### Matriz de configuración por módulo

| Módulo | `API_BASE_URL` | `FUNC_CITAS` | `FUNC_TRAMITES` | `FUNC_PAGOS` | `FUNC_BFF` | `FUNC_HISTORIAL` |
| --- | --- | --- | --- | --- | --- | --- |
| **m1** | `http://localhost:8081` | `true` | `false` | `false` | `false` | `false` |
| **m2** | `http://localhost:8080` | `true` | `true` | `false` | `true` | `false` |
| **m3 / m4** | `http://localhost:8080` | `true` | `true` | `true` | `true` | `true` |

---

## Ejecución con Docker (Modo Oficial de Clase)

### 1. Construir la imagen

Desde la raíz del repositorio:

```bash
docker build -t consular-frontend frontend/
```

### 2. Ejecutar para el Módulo 1 (`MODULO=m1`, puerto `:4200`)

```bash
docker run -d --name m1-frontend \
  -p 4200:80 \
  -e API_BASE_URL=http://localhost:8081 \
  -e MODULO=m1 \
  -e FUNC_CITAS=true \
  -e FUNC_TRAMITES=false \
  -e FUNC_PAGOS=false \
  -e FUNC_BFF=false \
  -e FUNC_HISTORIAL=false \
  consular-frontend
```

Abrir en el navegador: `http://localhost:4200`.

---

## Ejecución en Desarrollo Local (Opcional, con Node.js 22+)

Si se desea modificar o probar el frontend fuera de Docker:

```bash
cd frontend
npm install
npm run lint
npm start
```

