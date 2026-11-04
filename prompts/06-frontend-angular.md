# Prompt 06 — Frontend Angular + PrimeNG

> Rama `main`. Referencias: regla 05 (completa), contrato (secciones 4 y 6).

## Objetivo

Crear `frontend/`, la aplicación web ciudadana compartida por todos los módulos. Una sola imagen Docker configurable en tiempo de ejecución. Implementar **todas** las pantallas de la regla 05; las de M2+ se ocultan según `funcionalidades` en `config.json`.

## Tareas

1. Generar el proyecto Angular (última versión estable) con componentes standalone, routing, SCSS, ESLint. Instalar PrimeNG y PrimeIcons con un tema claro neutro. Configurar locale `es-PE`.
2. `core/config/ConfigService` que carga `/assets/config.json` con `provideAppInitializer` (o el mecanismo vigente) antes de iniciar.
3. Interceptores: `ErrorInterceptor` (convierte `ProblemDetail` en mensajes), `IdempotenciaInterceptor` (agrega `Idempotency-Key` a `POST /api/pagos` si no existe).
4. Modelos TypeScript que reflejen exactamente los DTO del contrato.
5. Pantallas y elementos didácticos de la regla 05:
   - Reservar cita en pasos (`p-steps` o `p-stepper`): oficina → fecha (`p-datepicker` limitado a mañana…30 días) → cupo (botones con libres/capacidad) → datos (DNI, nombres, email, tipo de trámite) → confirmación con código, estado y **estado de notificación**.
   - Disponibilidad con etiqueta de **origen** y antigüedad (CAP).
   - Consulta y cancelación de citas.
   - Inicio con tarjeta "Sistema en ejecución" (variante, arquitectura interna, modo de consistencia) cuando el módulo es m1.
   - Pantallas de trámites, pago, consulta con sondeo e historial, mi espacio (BFF) — activas según configuración.
6. Docker: `Dockerfile` multi-etapa, `nginx.conf`, `docker-entrypoint.d/40-config.sh` que genera `config.json` con `envsubst` a partir de `API_BASE_URL`, `MODULO`, `FUNC_CITAS`, `FUNC_TRAMITES`, `FUNC_PAGOS`, `FUNC_BFF`, `FUNC_HISTORIAL`.
7. Pruebas unitarias de `ConfigService`, servicio de citas y componente de reserva. `npm run lint` limpio.
8. `frontend/README.md`: cómo funciona la configuración por módulo, cómo ejecutar en desarrollo (opcional, con Node) y que en clase se usa solo la imagen Docker.

## Criterios de aceptación

- `docker build -t consular-frontend frontend/` termina sin errores.
- Con el monolito del prompt 02 en `:8081` y el frontend en `:4200` (`MODULO=m1`), se reserva una cita de punta a punta y se ve el estado de notificación.
- Con el monolito en modo `AP` y la red de datos desconectada, la pantalla de disponibilidad muestra "Caché – actualizado hace N s".
- Un error `409` muestra el `title` y `detail` del `ProblemDetail`.
- Las rutas de trámites no aparecen en el menú con `MODULO=m1`.

## Commit

`feat(frontend): aplicación web ciudadana`
