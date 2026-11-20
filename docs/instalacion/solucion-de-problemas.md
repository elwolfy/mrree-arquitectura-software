# Guía de Solución de Problemas Frecuentes (*Troubleshooting*)

Esta guía recopila los problemas más habituales en estaciones de trabajo Windows/macOS/Linux al trabajar con Docker Compose, PostgreSQL, RabbitMQ, Maven y Eclipse, junto con su solución inmediata.

---

## 1. Error de Puerto Ocupado (`Bind for 0.0.0.0:5432 failed: port is already allocated`)

### Síntoma
Al ejecutar `.\scripts\levantar.ps1`, Docker Compose falla al iniciar `postgres`, `rabbitmq`, `gateway` o `frontend` indicando que el puerto (`5432`, `5672`, `8080`, `8081`, `4200`, etc.) ya está en uso.

### Causa
- Quedó otro módulo del curso levantado en segundo plano (por ejemplo, `m1-estilos` sigue activo cuando intenta levantar `m2-integracion`).
- O bien tiene un servicio local de PostgreSQL o RabbitMQ instalado como servicio de Windows en su máquina host.

### Solución
1. Verifique qué contenedores están corriendo y detenga el módulo anterior:
   ```powershell
   docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
   ```
2. Ejecute el script de limpieza del módulo que estaba activo:
   ```powershell
   cd ..\m1-estilos
   .\scripts\limpiar.ps1 -Forzar
   ```
3. Si el puerto `5432` sigue ocupado sin contenedores Docker activos, verifique qué proceso de Windows lo retiene y detenga el servicio local de PostgreSQL:
   ```powershell
   netstat -ano | findstr :5432
   Stop-Service -Name "postgresql*" -Force
   ```

---

## 2. Error `/usr/bin/env: 'sh\r': No such file or directory` o Falla en `01-esquemas.sql`

### Síntoma
Un script `.sh` o un script de inicialización de contenedor falla mencionando `\r` al final de línea.

### Causa
El repositorio se clonó en Windows con `core.autocrlf=true`, convirtiendo los finales de línea Unix (`LF`) a Windows (`CRLF`).

### Solución
Ejecute en PowerShell desde la raíz del repositorio:

```powershell
git config --global core.autocrlf false
git config --local core.autocrlf false
git rm --cached -r .
git reset --hard
```

> [!CAUTION]
> Ejecute `git rm --cached -r .` y `git reset --hard` **únicamente** si no tiene cambios locales sin guardar (*uncommitted changes*). Si ya tiene avances de una asignación, guárdelos primero en un commit o con `git stash`.

---

## 3. Error de Flyway (`Validate failed: Migrations have failed validation` o `Checksum mismatch`)

### Síntoma
Un servicio Java no pasa a estado `healthy` y al revisar `docker compose logs <servicio>` aparece `FlywayValidateException`.

### Causa
El volumen persistente de PostgreSQL (`postgres_data`) conserva tablas o metadatos (`flyway_schema_history`) de una ejecución anterior cuyos scripts SQL cambiaron.

### Solución
Limpie los contenedores y volúmenes del módulo actual y vuelva a levantarlo para que PostgreSQL se inicialice en limpio:

```powershell
.\scripts\limpiar.ps1 -Forzar
.\scripts\levantar.ps1
```

---

## 4. Lentitud o Consumo Elevado de Memoria en Windows (`VmmemWSL`)

### Síntoma
El proceso `VmmemWSL` en el Administrador de tareas supera los 6 GB de RAM después de varias horas de trabajo o al levantar varios módulos seguidos.

### Solución
1. Detenga y limpie el módulo activo con `.\scripts\limpiar.ps1 -Forzar`.
2. Verifique que haya creado el archivo `$env:USERPROFILE\.wslconfig` con `memory=6GB` (como se indica en [02-docker-desktop.md](02-docker-desktop.md)).
3. Si necesita liberar inmediatamente la caché de páginas del kernel Linux de WSL 2, cierre Docker Desktop, ejecute en PowerShell:
   ```powershell
   wsl --shutdown
   ```
   y vuelva a abrir Docker Desktop.

---

## 5. `Testcontainers` Falla al Ejecutar `mvn verify` (`Could not find a valid Docker environment`)

### Síntoma
Al ejecutar las pruebas de integración (`CitaIntegracionTest`, `TramiteIntegracionTest`, `RedisCacheConsultaTramiteAdapterTest`, etc.) desde Eclipse, aparece `IllegalStateException: Could not find a valid Docker environment`.

### Causa
Docker Desktop está cerrado, o bien usa Rancher/Podman Desktop sin exposición del socket de Docker.

### Solución
1. Verifique que Docker responda en la terminal con `docker info`.
2. En Docker Desktop para Windows, abra **Settings > General** y verifique que esté marcada la opción **Expose daemon on tcp://localhost:2375 without TLS** solo si su cliente local no detecta el named pipe `npipe:////./pipe/docker_engine`, o asegúrese de ejecutar Eclipse con el mismo usuario que inició Docker Desktop.

---

## 6. La Primera Compilación con Maven Tarda Varios Minutos

### Síntoma
La primera vez que ejecuta `.\scripts\levantar.ps1` o `mvn verify` en contenedor, la descarga de dependencias de Maven Central demora entre 2 y 4 minutos.

### Explicación y Recomendación
- Es completamente normal en la primera ejecución.
- Los `Dockerfile` de todos los servicios utilizan montajes de caché de BuildKit (`--mount=type=cache,target=/root/.m2`) y los comandos Maven en contenedor usan el volumen nombrado `-v mre-m2:/root/.m2`.
- Gracias a esto, **a partir de la segunda ejecución las dependencias ya están en caché local** y las compilaciones toman entre 10 y 25 segundos.

---

## 7. Comandos Útiles de Diagnóstico Rápido

| Qué desea diagnosticar | Comando en PowerShell / Bash |
| --- | --- |
| Ver el estado de salud (`healthy`/`unhealthy`) de un módulo | `docker compose ps` |
| Seguir los logs en vivo de un microservicio específico | `docker compose logs -f tramites-service` |
| Ver las últimas 80 líneas de log de un contenedor que falló | `docker compose logs --tail 80 citas-service` |
| Medir el consumo real de CPU y RAM de cada contenedor | `docker stats --no-stream` |
| Inspeccionar las tablas de PostgreSQL desde la terminal | `docker exec -it m3-postgres psql -U postgres -d consular -c "\dn+"` |
| Validar el modelo C4 (`workspace.dsl`) sin levantar M4 | `docker run --rm -v "${PWD}/docs/c4:/usr/local/structurizr" structurizr/cli:2025.11.09 validate -workspace /usr/local/structurizr/workspace.dsl` |

