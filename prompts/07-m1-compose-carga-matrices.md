# Prompt 07 — M1: Docker Compose con perfiles, pruebas de carga y matrices

> Rama `main`. Referencias: regla 03, contrato (secciones 3, 4.2, 4.3 y 8), plan sección 7 (1.1–1.4) y sección 8 (S1, S2). **No modifiques `m2-integracion`**. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Objetivo

Dejar `m1-estilos` listo para las sesiones 1 y 2: un único `docker-compose.yml` con perfiles para cada variante, scripts robustos para participantes en Windows, herramientas de medición y las matrices que el participante completa en clase. Los nombres y comandos deben coincidir **exactamente** con los que citan la presentación, el material de estudio y el Manual de instalación del Módulo 1.

## Tareas

### 1. `m1-estilos/docker-compose.yml`

Reemplaza todos los `docker-compose.dev.yml` temporales de los prompts 02–06, que se **eliminan** (actualizar cualquier README o script que los mencione).

- `name: m1` en la primera línea. Ningún script usa `-p` ni otro nombre de proyecto.
- Redes con nombre fijo: `frontal` (`name: m1_frontal`) y `datos` (`name: m1_datos`). Volúmenes `m1_postgres_data`.
- Servicios sin perfil (siempre):
  - `postgres` → contenedor `m1-postgres`, `postgres:16-alpine`, initdb con un esquema y usuario por módulo, healthcheck.
  - `mailpit` → `m1-mailpit`, 1025/8025.
  - `frontend` → `m1-frontend`, 4200, `MODULO=m1`, `API_BASE_URL=http://localhost:8081`, `FUNC_CITAS=true`, resto `false`.
- Perfil `monolito`: `monolito` → `m1-monolito`, 8081; variables `CONSISTENCIA_MODO`, `NOTIFICACIONES_CANAL`, `NOTIFICACIONES_SIMULAR_FALLA` leídas de `.env`.
- Perfil `clean`: `clean` → `m1-clean`, 8081, mismas variables.
- Perfil `microservicios`: `citas` → `m1-citas` (8081) y `notificaciones` → `m1-notificaciones` (8084).
- Perfil `serverless`: `fn-consultas` → `m1-fn-consultas`, `fn-reservar` → `m1-fn-reservar`, `fn-notificar` → `m1-fn-notificar` (misma imagen) y `enrutador` → `m1-enrutador` (8081).
- Perfil `carga`: `k6` (`grafana/k6`), monta `../docs/pruebas/k6`, ejecuta el script indicado en `K6_SCRIPT` (por defecto `citas.js`) contra `OBJETIVO` (por defecto `http://host.docker.internal:8081`).
- Healthcheck en **todos** los servicios de aplicación (`/actuator/health`; en el enrutador, `/api/sistema/info`).
- `.env` y `.env.example` con `CONSISTENCIA_MODO=CP`, `NOTIFICACIONES_CANAL=CONSOLA`, `NOTIFICACIONES_SIMULAR_FALLA=false`.
- Comando de referencia del material del curso, que debe funcionar tal cual desde `m1-estilos`: `docker compose --profile monolito up -d --build`.

### 2. Scripts (`m1-estilos/scripts/`, versiones `.ps1` y `.sh` equivalentes)

Requisitos comunes a todos los scripts:

1. Verificar con `docker info` que Docker está en ejecución. Si no responde: mensaje "Docker Desktop no está en ejecución: ábralo y espere 'Engine running'" y salir con código 1.
2. Cortar con mensaje claro y código 1 si `docker compose` devuelve error. Nunca continuar esperando contenedores que no se crearon.
3. Al esperar el estado saludable, usar `{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}` y un tiempo máximo (180 s) con mensaje de qué contenedor no llegó a `healthy` y cómo ver su log.
4. Detectar si el puerto 8081 o 4200 está ocupado por otro módulo (por ejemplo `m2-citas`) y pedir detenerlo indicando el script exacto (`..\m2-integracion\scripts\detener.ps1`).
5. Mensajes en español, sin acentos problemáticos en consolas Windows antiguas (probar en PowerShell 5.1 y 7).

| Script | Acción |
| --- | --- |
| `levantar` `<monolito\|clean\|microservicios\|serverless>` (por defecto `monolito`) | Detiene cualquier otra variante, ejecuta `up -d --build` del perfil elegido, espera `healthy` e imprime la tabla de URLs: frontend `localhost:4200`, API `localhost:8081`, Swagger `localhost:8081/swagger-ui.html` (en serverless indicar que no aplica), Mailpit `localhost:8025` |
| `detener` | `docker compose --profile "*" stop` sin borrar datos |
| `limpiar` | `docker compose --profile "*" down -v --remove-orphans` (borra datos; pide confirmación salvo con `-Forzar` / `--forzar`) |
| `medir` | Tiempo de arranque leído de los logs (Spring Boot `Started … in N seconds`; serverless `Función lista en N ms`), `docker stats --no-stream` filtrado por `m1-`, número de contenedores y memoria total |
| `carga` `[citas\|pico]` | Ejecuta el perfil `carga` y resume p95, solicitudes por segundo y tasa de error |
| `simular-particion` / `restaurar-red` | `docker network disconnect/connect m1_datos m1-monolito` (o `m1-clean` si es la variante activa); detecta cuál está corriendo |
| `escalar` `<n>` | Solo microservicios. Si agregar un balanceador complica la solución, **omitir el script** y documentar la demostración de escalabilidad solo con `carga pico` |

Agregar o verificar en `.gitattributes` que `*.sh` use fin de línea `LF` y `*.ps1` `CRLF`.

### 3. Pruebas de carga (`docs/pruebas/k6/`)

- `citas.js`: 50 usuarios virtuales durante 60 s; 80 % consultas (oficinas y disponibilidad), 20 % reservas con DNI `40000001`–`40000050` y cupos libres obtenidos de la disponibilidad antes de reservar.
- Umbrales informativos, no bloqueantes: p95 < 2 s, errores < 5 %. Un `409` por cita duplicada o un cupo lleno cuenta como respuesta esperada, no como error.
- Resumen final en español (`handleSummary`): p95, solicitudes por segundo y tasa de error.
- `citas-pico.js`: rampa de 10 a 100 usuarios (escenario de escalabilidad, 1.1).

### 4. Matrices (`docs/matrices/`)

- `m1-estilos.md`: matriz de criterios (plan, sección 7, 1.2) y matriz de medición vacía (arranque, memoria, contenedores, p95, solicitudes/s, comportamiento de la reserva ante falla de notificaciones) con instrucciones para obtener cada valor con los scripts. Incluir la fila de comportamiento esperado del contrato 4.3 como columna "Esperado" para que el participante compare.
- `m1-escenarios-calidad.md`: plantilla de escenario de seis partes, un ejemplo resuelto del caso y árbol de utilidad para completar.
- `m1-cap-por-operacion.md`: tabla CAP por operación (plan, 1.4) con columnas "Decisión", "Justificación" y "Observado en la prueba de partición". Incluir la fila **"Reprogramar cita"** con decisión y justificación **vacías** (es insumo de la tarea conceptual de A1; no la resuelvas).
- `m1-hexagonal-vs-clean.md` ya existe (prompt 03): solo verificar que sus enlaces siguen vigentes.

### 5. README de `m1-estilos`

Secciones:

1. Qué contiene y conceptos por sesión (S1: 1.1–1.2; S2: 1.3–1.4 y 2.1).
2. Cómo levantar cada variante (con los scripts y con el comando `docker compose --profile …` equivalente) y la advertencia de que las variantes y M2 son mutuamente excluyentes por los puertos.
3. Tabla de URLs.
4. Experimentos guiados paso a paso:
   - Falla de notificaciones en las tres variantes (`NOTIFICACIONES_SIMULAR_FALLA=true` en monolito; `docker stop m1-notificaciones`; `docker stop m1-fn-notificar`).
   - Partición CP vs. AP con `simular-particion` y `CONSISTENCIA_MODO`.
   - Nueva estrategia de tarifa `REGISTRO_CONSULAR` (S2): verificar que en `main` `GET /api/tarifas?tipoTramite=REGISTRO_CONSULAR&pais=ES` responde error y que, tras agregar la clase, `git status` muestra un solo archivo nuevo.
   - Romper la regla de ArchUnit en monolito y en clean.
5. Modo híbrido en Eclipse (detener el contenedor de la variante y ejecutar con perfil `local`).
6. Memoria y arranque medidos por variante (valores reales obtenidos con `medir`).
7. Solución de problemas en Windows: Docker Desktop detenido, WSL 2, puertos ocupados por M2, fin de línea, restos de ejecuciones anteriores (`limpiar`).

## Criterios de aceptación

- No queda ningún `docker-compose.dev.yml` en el repositorio; `m2-integracion` sin cambios (`git status`).
- `/verificar-modulo m1-estilos monolito`, luego `clean`, `microservicios` y `serverless`: todos pasan, ejecutados de a uno con `levantar`.
- Con Docker Desktop detenido, `levantar` muestra el mensaje claro y termina con código 1.
- Con `m2-citas` en ejecución, `levantar` advierte el conflicto de puerto en lugar de fallar con un error de Docker.
- En cada variante, el frontend en `http://localhost:4200` reserva una cita con el DNI `40000001`.
- `carga` produce el resumen con p95 en monolito, microservicios y serverless; los valores se registran en el README.
- `simular-particion` con `CONSISTENCIA_MODO=AP` → disponibilidad desde caché en el frontend ("actualizado hace N s"); con `CP` → mensaje de servicio no disponible; `restaurar-red` recupera el servicio.
- `GET /api/tarifas?tipoTramite=REGISTRO_CONSULAR&pais=ES` responde error en `main` (laboratorio de S2).
- La fila "Reprogramar cita" de `m1-cap-por-operacion.md` está sin resolver.
- Al terminar, ejecutar `limpiar -Forzar` para dejar los contenedores detenidos y eliminados.

## Commit

`feat(m1): compose, pruebas de carga y matrices` · Etiqueta `s1` sobre este commit.
