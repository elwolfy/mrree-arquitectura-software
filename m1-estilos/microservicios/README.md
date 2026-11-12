# Variante Microservicios (`m1-estilos/microservicios`)

Implementación del estilo de **Microservicios** del Módulo 1 (**Sesión S1**, sección 1.2 del plan) para la **Plataforma Digital de Servicios Consulares y de Legalización** (UTEC Posgrado – MRREE).

La misma funcionalidad de reserva de citas y envío de avisos del monolito modular se reparte aquí en **dos servicios independientes**, cada uno con su propio proceso, contenedor Docker y esquema exclusivo en PostgreSQL:

- **`citas-service` (`consular-m1-citas-service`, puerto `8081`)**: Expone la API pública de M1 en `http://localhost:8081` para que el frontend Angular y los scripts k6 funcionen sin cambios.
- **`notificaciones-service` (`consular-m1-notificaciones-service`, puerto `8084`)**: Recibe solicitudes `POST /api/avisos` (`202 Accepted`) y consulta avisos en `GET /api/avisos?referencia=`.

---

## 1. Diagrama de Arquitectura (M1 Microservicios)

```text
                        +---------------------------------------------------+
                        |            Cliente (Frontend :4200 / k6)          |
                        +---------------------------------------------------+
                                                  |
                                                  | HTTP REST (:8081)
                                                  v
+---------------------------------------------------------------------------------------------------+
| Red Docker: m1_frontal / m1_datos                                                                 |
|                                                                                                   |
|  +---------------------------------------------+     HTTP POST /api/avisos     +---------------+  |
|  |                m1-citas (:8081)             |  Timeout 2s + CircuitBreaker  |m1-notificaciones|
|  |         (consular-m1-citas-service)         | ----------------------------> |    (:8084)    |  |
|  |                                             |   Fallback -> PENDIENTE (201) |               |  |
|  |  adapter.in.web -> application -> domain    |                               |  Hexagonal    |  |
|  |  adapter.out.persistence  adapter.out.http  |                               +-------+-------+  |
|  +--------------------+------------------------+                                       |          |
|                       |                                                                |          |
|                       | JDBC (usuario: citas_app)            JDBC (notificaciones_app) |          |
|                       v                                                                v          |
|  +---------------------------------------------------------------------------------------------+  |
|  |                              m1-postgres (PostgreSQL 16 :5432)                              |  |
|  |                     Esquema: citas                        Esquema: notificaciones           |  |
|  +---------------------------------------------------------------------------------------------+  |
+---------------------------------------------------------------------------------------------------+
```

---

## 2. Diferencias Clave frente al Monolito Modular (`monolito-modular`)

| Dimensión | Monolito Modular (`m1-monolito`) | Microservicios (`m1-citas` + `m1-notificaciones`) |
| --- | --- | --- |
| **Despliegue y procesos** | 1 contenedor Java (`m1-monolito`) | 2 contenedores Java independientes (`m1-citas` y `m1-notificaciones`) |
| **Aislamiento de base de datos** | Conexión compartida a los esquemas `citas`, `tramites`, `notificaciones` | Credenciales y esquemas separados: `citas_app` (`citas`) y `notificaciones_app` (`notificaciones`) |
| **Compartición de código** | Paquete interno `compartido` dentro del mismo artefacto | **Duplicación controlada** de objetos de valor (`Dni`, `Email`, excepciones) sin librería común para evitar acoplamiento de despliegue |
| **Comunicación Citas → Notificaciones** | Invocación Java en memoria dentro de la misma transacción `@Transactional` | Llamada de red HTTP (`RestClient`) hacia `http://notificaciones:8084/api/avisos` |
| **Efecto de caída de Notificaciones** | La reserva **falla (`500`)** y hace rollback del cupo y de la cita | El Circuit Breaker activa el fallback: la reserva **se confirma (`201`)** con `notificacion: PENDIENTE` en `< 3 s` |
| **Alcance funcional en M1** | Incluye `/api/tarifas` y selector CAP (`CP`/`AP`) | Expone toda la API de citas y oficinas en `:8081` **excepto** `/api/tarifas` (no hay módulo de tarifas en esta variante) y opera únicamente en modo `CP` (`variante: MICROSERVICIOS`) |

> **Nota pedagógica sobre duplicación controlada:** En esta variante no existe un módulo Maven `common` compartido entre `citas-service` y `notificaciones-service`. En microservicios, compartir una librería de dominio acopla los ciclos de compilación y despliegue de ambos equipos; por ello cada servicio mantiene su propio modelo interno.

---

## 3. ¿Dónde está configurado el Circuit Breaker?

1. **Adaptador de salida HTTP (`citas-service`)**:
   - Clase: `pe.utec.consular.citas.adapter.out.http.NotificacionHttpAdapter`
   - Anotación: `@CircuitBreaker(name = "notificaciones", fallbackMethod = "fallbackNotificarReserva")`
   - Timeout de red: `SimpleClientHttpRequestFactory` con `connectTimeout = 2000 ms` y `readTimeout = 2000 ms`.
   - Fallback (`fallbackNotificarReserva`): registra la advertencia en el log y devuelve `EstadoNotificacion.PENDIENTE`, permitiendo que `RegistrarCitaService` confirme la cita con HTTP `201 Created`.
2. **Configuración (`citas-service/src/main/resources/application.yml`)**:
   ```yaml
   notificaciones:
     base-url: ${NOTIFICACIONES_URL:http://notificaciones:8084}
     timeout-ms: ${NOTIFICACIONES_TIMEOUT_MS:2000}

   resilience4j:
     circuitbreaker:
       instances:
         notificaciones:
           slidingWindowType: COUNT_BASED
           slidingWindowSize: 5
           minimumNumberOfCalls: 3
           failureRateThreshold: 50
           waitDurationInOpenState: 10s
   ```

---

## 4. Experimento Guiado (S1): Detener `m1-notificaciones` y Observar Degradación Elegante

### Paso 1: Levantar la variante microservicios
Desde la carpeta `m1-estilos`:
```powershell
docker compose -f docker-compose.dev.yml --profile microservicios up -d --build
```

### Paso 2: Verificar información del sistema y reserva normal (`ENVIADA`)
```powershell
curl -i http://localhost:8081/api/sistema/info
# {"variante":"MICROSERVICIOS","arquitecturaInterna":"HEXAGONAL","modoConsistencia":"CP","version":"1.0.0"}

curl -i -X POST http://localhost:8081/api/citas `
  -H "Content-Type: application/json" `
  -d '{\"dni\":\"40000001\",\"nombres\":\"Ana Pérez\",\"email\":\"ana@correo.lab\",\"oficinaId\":\"MAD\",\"fecha\":\"2026-11-20\",\"hora\":\"08:00\",\"tipoTramite\":\"LEGALIZACION\"}'
# HTTP/1.1 201 Created -> "notificacion": "ENVIADA"
```

También puede consultar el aviso persistido en `notificaciones-service`:
```powershell
curl -i "http://localhost:8084/api/avisos?referencia=CIT-2026-000001"
```

### Paso 3: Detener `m1-notificaciones` y reservar una nueva cita (`PENDIENTE` en < 3 s)
```powershell
docker stop m1-notificaciones

curl -i -X POST http://localhost:8081/api/citas `
  -H "Content-Type: application/json" `
  -d '{\"dni\":\"40000002\",\"nombres\":\"Luis Gómez\",\"email\":\"luis@correo.lab\",\"oficinaId\":\"MAD\",\"fecha\":\"2026-11-20\",\"hora\":\"08:30\",\"tipoTramite\":\"APOSTILLA\"}'
# HTTP/1.1 201 Created en < 3 segundos -> "estado": "RESERVADA", "notificacion": "PENDIENTE"
```

### Paso 4: Restaurar o detener el entorno
```powershell
docker compose -f docker-compose.dev.yml --profile microservicios down -v
```

---

## 5. Ejecución en Modo Híbrido desde Eclipse

1. Levante PostgreSQL y Mailpit desde `m1-estilos`:
   ```powershell
   docker compose -f docker-compose.dev.yml up -d postgres mailpit
   ```
2. En Eclipse, importe `m1-estilos/microservicios/notificaciones-service` y `m1-estilos/microservicios/citas-service` como *Existing Maven Projects*.
3. Ejecute `NotificacionesApplication` y `CitasApplication` con *Run As → Spring Boot App* y perfil activo `local`.

