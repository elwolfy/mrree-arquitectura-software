# Prompt 04 — M1: Variante microservicios

> Rama `main`. Referencias: contrato (secciones 3–5, 4.3), reglas 01–04.

## Objetivo didáctico

Misma funcionalidad que el monolito (citas + notificaciones) repartida en **dos servicios independientes**, cada uno con su esquema y su contenedor, para medir en S1: arranque, memoria, número de contenedores, latencia p95, efecto de detener notificaciones y pasos para desplegar un cambio (plan, sección 7, 1.2).

## Alcance

```text
m1-estilos/microservicios/
├── citas-service/            puerto 8081 — artifactId consular-m1-citas-service
└── notificaciones-service/   puerto 8084 — artifactId consular-m1-notificaciones-service
```

### citas-service

- Hexagonal, mismos paquetes y casos de uso que el módulo `citas` del monolito (puede partir de una copia; **no** compartir código por librería común: la duplicación controlada es parte de la lección y se comenta en el README).
- Expone la API de M1 del contrato **excepto** `/api/tarifas` (en esta variante no hay módulo de tarifas; documentarlo).
- `NotificacionPort` implementado por `NotificacionHttpAdapter` (RestClient) hacia `http://notificaciones:8084/api/avisos`, con timeout 2 s y circuit breaker Resilience4j (`notificaciones`). Fallback: la reserva se confirma con `notificacion: PENDIENTE` y se registra en log.
- Sin modo CAP (solo `CP`); `sistema/info` → `variante: MICROSERVICIOS`.
- Esquema `citas`; usuario `citas_app`.

### notificaciones-service

- Hexagonal. `POST /api/avisos` (`AvisoRequest {destinatario, asunto, cuerpo, referencia}` → `202`), `GET /api/avisos?referencia=`.
- Canales consola/correo y `notificaciones.simular-falla` como en el monolito.
- Esquema `notificaciones`; usuario `notificaciones_app`.

### Pruebas

- ArchUnit en ambos servicios.
- citas-service: prueba de integración con WireMock simulando notificaciones caído → `201` con `PENDIENTE`.
- notificaciones-service: integración básica.

### Docker

Dockerfiles según regla 03 (contexto `m1-estilos`). Agregar al `docker-compose.dev.yml` temporal para verificar.

### README

`m1-estilos/microservicios/README.md`: diagrama ASCII de los dos servicios, diferencias con el monolito, dónde está el circuit breaker, cómo detener `m1-notificaciones` para el experimento.

## Criterios de aceptación

- `mvn -B verify` pasa.
- Reserva normal → `201` con `notificacion: ENVIADA`.
- `docker stop m1-notificaciones` y nueva reserva → `201` con `notificacion: PENDIENTE` en < 3 s.
- Frontend/k6 pueden usar `http://localhost:8081` sin cambios.

## Commit

`feat(m1): variante microservicios`
