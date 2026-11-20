# `citas-service` — Microservicio de Citas Consulares (Módulo 2)

Microservicio autónomo para el Bounded Context **Citas** (`pe.utec.consular.citas`), estructurado con **Arquitectura Hexagonal** y desacoplado de otros servicios mediante eventos asíncronos sobre **RabbitMQ** (exchange `consular.eventos`, routing key `cita.reservada`).

> **Nota didáctica (DDD - Relación entre contextos):** Este servicio duplica deliberadamente sus propios objetos de valor (`Dni`, `Email`, `TipoTramite`) sin compartir una librería de dominio común con otros servicios, evitando así el acoplamiento de un *Shared Kernel* accidental.

## Puertos y Endpoints

- **Puerto HTTP:** `8081`
- **Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8081/v3/api-docs`
- **Healthcheck:** `http://localhost:8081/actuator/health`

## Ejecución en modo híbrido desde Eclipse

1. Levantar la infraestructura con Docker Compose desde `m2-integracion/`:
   ```powershell
   .\scripts\levantar.ps1
   ```
2. Detener únicamente el contenedor del servicio si desea reemplazarlo desde el IDE:
   ```powershell
   docker stop m2-citas
   ```
3. En **Spring Tools for Eclipse**, clic derecho sobre `CitasApplication.java` → *Run As* → *Spring Boot App* con el perfil activo **`local`** (`-Dspring.profiles.active=local`).

