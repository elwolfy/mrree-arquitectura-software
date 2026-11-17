# Matriz S2: Decisiones CAP / PACELC por Operación (`m1-estilos`)

> **Sesión:** S2 — Sección 1.4 del sílabo (*Teorema CAP y decisiones de consistencia por operación de negocio*).

En una misma plataforma —e incluso dentro de un mismo servicio— el arquitecto no elige `CP` o `AP` de forma monolítica para todo el sistema, sino **por operación de negocio**, evaluando el costo de mostrar un dato desactualizado frente al costo de rechazar la operación durante una partición de red.

---

## 1. Cómo Ejecutar el Experimento de Partición en Clase

1. Levante el monolito (en modo `CP` por defecto, o cambiando `CONSISTENCIA_MODO=AP` en `m1-estilos/.env`):
   ```powershell
   .\m1-estilos\scripts\levantar.ps1 monolito
   ```
2. Consulte la disponibilidad de Madrid en el navegador (`http://localhost:4200/citas/nueva`) o por API para poblar la caché Caffeine:
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-18"
   ```
3. Simule la partición de red entre el servicio y PostgreSQL:
   ```powershell
   .\m1-estilos\scripts\simular-particion.ps1
   ```
4. Pruebe la consulta de disponibilidad y el registro de una cita durante la partición, anote lo observado en la tabla inferior y finalmente restaure la red:
   ```powershell
   .\m1-estilos\scripts\restaurar-red.ps1
   ```

---

## 2. Tabla de Decisiones CAP por Operación

| Operación | Decisión (`CP` / `AP`) | Justificación | Observado en la prueba de partición |
| --- | --- | --- | --- |
| **Reservar cita** (`POST /api/citas`) | **CP** | La asignación de un cupo modifica estado crítico compartido y exige bloqueo pesimista (`SELECT ... FOR UPDATE`) y verificación de no duplicidad. Aceptar reservas a ciegas sin base de datos provocaría sobreventa de turnos consulares e incumplimiento legal de aforo. | Ante la partición de `m1_datos`, falla rápidamente en `< 2 s` con HTTP `503 Service Unavailable` (`base-de-datos-no-disponible`), tanto si el sistema está configurado en `CP` como en `AP`. |
| **Consultar disponibilidad** (`GET /api/oficinas/{id}/disponibilidad`) | *(completar en clase)* | *(completar en clase: comparar el valor para el ciudadano de ver cupos con unos segundos de antigüedad vs. recibir una pantalla de error)* | *(completar en clase probando con `CONSISTENCIA_MODO=CP` y luego con `CONSISTENCIA_MODO=AP`)* |
| **Enviar notificación** (al confirmar una cita) | *(completar en clase)* | *(completar en clase: ¿debe abortarse la cita del ciudadano si el servidor de correo o servicio de avisos tarda o está caído?)* | *(completar en clase comparando el monolito frente a microservicios/serverless)* |
| **Reprogramar cita** (`PUT /api/citas/{id}/reprogramacion`) — *Asignación A1* | | | |

> **Nota sobre la fila "Reprogramar cita":** Las columnas *Decisión* y *Justificación* de esta operación se encuentran **deliberadamente vacías** porque constituyen la tarea conceptual que el participante debe fundamentar en la **Asignación A1** (`docs/adr/ADR-001-estilo-y-consistencia.md`).

