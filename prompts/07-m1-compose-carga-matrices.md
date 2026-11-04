# Prompt 07 — M1: Docker Compose con perfiles, pruebas de carga y matrices

> Rama `main`. Referencias: regla 03, contrato (secciones 3 y 4.3), plan sección 7 (1.1–1.4) y sección 8 (S1, S2).

## Objetivo

Dejar `m1-estilos` listo para clase: un único `docker-compose.yml` con perfiles para cada variante, herramientas de medición y las matrices que el participante completa en S1 y S2.

## Tareas

### 1. `m1-estilos/docker-compose.yml` (reemplaza los `docker-compose.dev.yml` temporales, que se eliminan)

- `name: m1`. Redes `frontal` y `datos`.
- Servicios sin perfil (siempre): `postgres` (`m1-postgres`, initdb, healthcheck), `mailpit` (`m1-mailpit`, 8025), `frontend` (`m1-frontend`, 4200, `MODULO=m1`, `API_BASE_URL=http://localhost:8081`, `FUNC_CITAS=true`, resto `false`).
- Perfil `monolito`: `monolito` (`m1-monolito`, 8081, variables `CONSISTENCIA_MODO`, `NOTIFICACIONES_CANAL`, `NOTIFICACIONES_SIMULAR_FALLA` leídas de `.env`).
- Perfil `clean`: `clean` (`m1-clean`, 8081).
- Perfil `microservicios`: `citas` (`m1-citas`, 8081), `notificaciones` (`m1-notificaciones`, 8084).
- Perfil `serverless`: `fn-consultas`, `fn-reservar`, `fn-notificar`, `enrutador` (`m1-enrutador`, 8081).
- Perfil `carga`: `k6` (`grafana/k6`), monta `../docs/pruebas/k6`, ejecuta `citas.js` contra `http://<punto-de-entrada>:8081` (variable `OBJETIVO`, por defecto `host.docker.internal:8081`).
- `.env` y `.env.example` con `CONSISTENCIA_MODO=CP`, `NOTIFICACIONES_CANAL=CONSOLA`, `NOTIFICACIONES_SIMULAR_FALLA=false`.
- Los perfiles de variantes son mutuamente excluyentes (todos publican 8081): documentarlo en el README y en los scripts.

### 2. Scripts (`m1-estilos/scripts/`, `.ps1` y `.sh`)

| Script | Acción |
| --- | --- |
| `levantar` `<monolito|clean|microservicios|serverless>` | `docker compose down` de otras variantes y `up -d --build` del perfil elegido; espera `healthy`; imprime URLs |
| `medir` | Imprime tiempo de arranque (desde logs), `docker stats --no-stream` filtrado por `m1-`, y número de contenedores |
| `carga` | Ejecuta el perfil `carga` y resume p95 y solicitudes/s |
| `simular-particion` / `restaurar-red` | `docker network disconnect/connect m1_datos m1-monolito` (o `m1-clean`) |
| `escalar` `<n>` | (solo microservicios) escala `citas` a n réplicas detrás de un nginx balanceador opcional — si añadir el balanceador complica, documentar la demostración solo con k6 y omitir el script |

### 3. Pruebas de carga (`docs/pruebas/k6/citas.js`)

- Escenario: 50 usuarios virtuales durante 60 s; 80 % consultas de disponibilidad y oficinas, 20 % reservas con DNI y cupos aleatorios válidos (consultar disponibilidad antes de reservar).
- Umbrales informativos (no bloqueantes): p95 < 2 s, errores < 5 %.
- Salida resumida en español: `p95`, solicitudes por segundo, tasa de error.
- Variante `citas-pico.js`: rampa 10 → 100 usuarios (escenario de atributo de escalabilidad, 1.1).

### 4. Matrices (`docs/matrices/`)

- `m1-estilos.md`: matriz de criterios (copiar del plan, sección 7, 1.2) + matriz de medición vacía con instrucciones de cómo obtener cada valor con los scripts.
- `m1-escenarios-calidad.md`: plantilla de escenario de seis partes, ejemplo del caso, árbol de utilidad para completar.
- `m1-cap-por-operacion.md`: tabla CAP por operación (plan, 1.4) con columnas para lo observado en la prueba de partición.
- `m1-hexagonal-vs-clean.md` ya existe (prompt 03).

### 5. README de `m1-estilos`

Secciones: qué contiene, conceptos por sesión (S1: 1.1–1.2; S2: 1.3–1.4–2.1), cómo levantar cada variante, URLs (frontend, API, Swagger, Mailpit), experimentos guiados paso a paso (detener notificaciones, partición CP/AP, nueva estrategia de tarifa, romper regla ArchUnit), modo híbrido en Eclipse, consumo de memoria medido, solución de problemas comunes en Windows (WSL 2, puertos ocupados, fin de línea).

## Criterios de aceptación

- `/verificar-modulo m1-estilos monolito`, luego `clean`, `microservicios` y `serverless`: todos pasan.
- En cada variante: el frontend en `http://localhost:4200` reserva una cita.
- `scripts/carga` produce un resumen con p95 en las tres variantes de estilo.
- `scripts/simular-particion` con `CONSISTENCIA_MODO=AP` → disponibilidad desde caché en el frontend; con `CP` → mensaje de servicio no disponible.
- Memoria total por variante registrada en el README.

## Commit

`feat(m1): compose, pruebas de carga y matrices`
