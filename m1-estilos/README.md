# Módulo 1 (`m1-estilos`) — Estilos Arquitectónicos, Arquitectura Limpia y Teorema CAP

Módulo correspondiente a las **Sesiones S1 y S2** del curso **Arquitectura de Software** (UTEC Posgrado – Caso integrador: *Plataforma Digital de Servicios Consulares y de Legalización*).

En esta carpeta conviven **cuatro implementaciones** de la misma API de negocio (`http://localhost:8081`), compartiendo el mismo visor ciudadano en Angular + PrimeNG (`http://localhost:4200`), la misma base de datos PostgreSQL 16 y la misma batería de pruebas de carga k6, con el propósito de comparar experimentalmente sus compromisos arquitectónicos (*trade-offs*).

---

## 1. Contenido del Módulo y Conceptos por Sesión

### Estructura de carpetas

```text
m1-estilos/
├── pom.xml                         # POM agregador Maven de los 5 artefactos Java de M1
├── docker-compose.yml              # Compose único (name: m1) con perfiles por variante y carga k6
├── .env / .env.example             # Variables pedagógicas: CONSISTENCIA_MODO, NOTIFICACIONES_CANAL, NOTIFICACIONES_SIMULAR_FALLA
├── initdb/01-esquemas.sql          # Creación de esquemas (citas, tramites, notificaciones) y usuarios por servicio
├── scripts/                        # Scripts .ps1 y .sh: levantar, detener, limpiar, medir, carga, simular-particion, restaurar-red
├── monolito-modular/               # Variante 1: Monolito modular con Arquitectura Hexagonal y Spring Modulith
├── comparativa-clean/              # Variante 2: Monolito con Clean Architecture (4 círculos concéntricos + Presenter)
├── microservicios/                 # Variante 3: citas-service (:8081) + notificaciones-service (:8084) con Resilience4j
└── serverless/                     # Variante 4: Funciones sin estado (Spring Cloud Function) + enrutador Nginx (:8081)
```

### Mapa de conceptos por sesión

| Sesión | Sección del Sílabo | Conceptos Demostrados en `m1-estilos` | Artefactos y Matrices de Apoyo |
| --- | --- | --- | --- |
| **Sesión 1 (S1)** | **1.1 Atributos de calidad** | Escenarios cuantificables de 6 partes (disponibilidad, latencia `p95`, escalabilidad, integridad de cupos con `SELECT ... FOR UPDATE`) y árbol de utilidad. | [`docs/matrices/m1-escenarios-calidad.md`](../docs/matrices/m1-escenarios-calidad.md)<br>[`docs/pruebas/k6/citas.js`](../docs/pruebas/k6/citas.js) y [`citas-pico.js`](../docs/pruebas/k6/citas-pico.js) |
| **Sesión 1 (S1)** | **1.2 Estilos arquitectónicos** | Comparación empírica entre **Monolito Modular**, **Microservicios** y **Serverless (FaaS)**: tiempo de arranque, huella de RAM, latencia y aislamiento ante fallas. | [`docs/matrices/m1-estilos.md`](../docs/matrices/m1-estilos.md)<br>`scripts/levantar`, `scripts/medir`, `scripts/carga` |
| **Sesión 2 (S2)** | **1.3 Hexagonal vs. Clean Architecture** | Contraste lado a lado entre Puertos y Adaptadores (`monolito-modular`) y los 4 círculos concéntricos con `InputBoundary`/`OutputBoundary`/`Presenter` (`comparativa-clean`), gobernados con **ArchUnit**. | [`docs/matrices/m1-hexagonal-vs-clean.md`](../docs/matrices/m1-hexagonal-vs-clean.md) |
| **Sesión 2 (S2)** | **1.4 Teorema CAP / PACELC** | Decisión de consistencia por operación: reserva siempre `CP` (`503` ante partición) vs. consulta de disponibilidad configurable en `CP` o `AP` (caché Caffeine con indicador de antigüedad). | [`docs/matrices/m1-cap-por-operacion.md`](../docs/matrices/m1-cap-por-operacion.md)<br>`scripts/simular-particion`, `scripts/restaurar-red` |
| **Sesión 2 (S2)** | **2.1 Patrones de diseño GoF** | **Strategy** (cálculo extensible de tarifas por trámite/país), **Observer** (eventos de dominio `CitaReservada`) y **Adapter** (canales `CONSOLA` y `CORREO` hacia Mailpit). | Paquetes `tramites.domain.tarifa` y `notificaciones.adapter` |

---

## 2. Cómo Levantar cada Variante

> [!WARNING]
> **Exclusión mutua de puertos (`:8081` y `:4200`):**
> Las cuatro variantes de `m1-estilos` publican la API en el puerto **`8081`** y el visor ciudadano en **`4200`**, los mismos puertos que utilizan los módulos `m2-integracion`, `m3-datos` y `m4-observabilidad`.
> - **No pueden ejecutarse dos variantes ni dos módulos al mismo tiempo.**
> - El script `levantar` detiene automáticamente cualquier otra variante previa de M1 y verifica que ningún contenedor de M2–M4 esté ocupando `:8081` o `:4200`.

Ubicado en la carpeta `m1-estilos`:

| Variante | Comando con Script (PowerShell) | Comando con Script (Bash) | Comando `docker compose` Equivalente |
| --- | --- | --- | --- |
| **Monolito Modular (Hexagonal)** | `.\scripts\levantar.ps1 monolito` | `./scripts/levantar.sh monolito` | `docker compose --profile monolito up -d --build` |
| **Clean Architecture** | `.\scripts\levantar.ps1 clean` | `./scripts/levantar.sh clean` | `docker compose --profile clean up -d --build` |
| **Microservicios** | `.\scripts\levantar.ps1 microservicios` | `./scripts/levantar.sh microservicios` | `docker compose --profile microservicios up -d --build` |
| **Serverless (FaaS)** | `.\scripts\levantar.ps1 serverless` | `./scripts/levantar.sh serverless` | `docker compose --profile serverless up -d --build` |

### Scripts adicionales de operación

```powershell
# Medir tiempos de arranque en logs y consumo de RAM en vivo (docker stats)
.\scripts\medir.ps1

# Ejecutar prueba de carga nominal (50 VUs por 60 s) o de pico (rampa 10 -> 100 VUs)
.\scripts\carga.ps1 citas
.\scripts\carga.ps1 pico

# Detener los contenedores conservando los datos en el volumen m1_postgres_data
.\scripts\detener.ps1

# Eliminar todos los contenedores, redes y volúmenes de datos de M1
.\scripts\limpiar.ps1 -Forzar
```

---

## 3. Tabla de URLs y Puertos

| Servicio / Interfaz | URL | Disponibilidad por Variante |
| --- | --- | --- |
| **Frontend Ciudadano (Angular + PrimeNG)** | http://localhost:4200 | Todas (`monolito`, `clean`, `microservicios`, `serverless`) |
| **API Pública de Citas y Oficinas** | http://localhost:8081/api/oficinas | Todas |
| **Información del Sistema en Ejecución** | http://localhost:8081/api/sistema/info | Todas |
| **Swagger UI (OpenAPI 3)** | http://localhost:8081/swagger-ui.html | `monolito`, `clean` y `microservicios` *(No aplica en `serverless`)* |
| **Mailpit (Bandeja Web de Correos)** | http://localhost:8025 | Todas (cuando `NOTIFICACIONES_CANAL=CORREO`) |
| **API Interna de Notificaciones** | http://localhost:8084/api/avisos | Solo en `microservicios` (`m1-notificaciones`) |

---

## 4. Experimentos Guiados Paso a Paso

### Experimento 1 (S1): Comportamiento de la Reserva ante Falla de Notificaciones (Contrato 4.3)

Este experimento demuestra por qué el acoplamiento síncrono dentro de una misma transacción difiere del aislamiento con *Circuit Breaker* o invocación asíncrona:

#### A) En el Monolito (`monolito` -> Falla con HTTP `500` y revierte la cita)
1. Edite `m1-estilos/.env` y cambie `NOTIFICACIONES_SIMULAR_FALLA=true`.
2. Levante el monolito:
   ```powershell
   .\scripts\levantar.ps1 monolito
   ```
3. Intente reservar una cita desde `http://localhost:4200/citas/nueva` o por consola:
   ```powershell
   $body = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"09:00","tipoTramite":"LEGALIZACION"}')
   Invoke-WebRequest -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body -UseBasicParsing
   ```
   **Resultado observado:** Responde **`500 Internal Server Error`** y la cita **no** se guarda (la transacción `@Transactional` hace rollback completo).
4. Restaure `NOTIFICACIONES_SIMULAR_FALLA=false` en `m1-estilos/.env`.

#### B) En Microservicios (`microservicios` -> Confirma con HTTP `201` y `notificacion: PENDIENTE`)
1. Levante la variante de microservicios:
   ```powershell
   .\scripts\levantar.ps1 microservicios
   ```
2. Detenga el contenedor de notificaciones para simular su caída:
   ```powershell
   docker stop m1-notificaciones
   ```
3. Reserve una cita desde `http://localhost:4200/citas/nueva` o por consola:
   **Resultado observado:** El *Circuit Breaker* (Resilience4j) con timeout de 2 s activa el *fallback* y responde **`201 Created`** en `< 3 s` con `"estado": "RESERVADA"` y **`"notificacion": "PENDIENTE"`**.

#### C) En Serverless (`serverless` -> Confirma con HTTP `201` y `notificacion: PENDIENTE` en ~300 ms)
1. Levante la variante serverless:
   ```powershell
   .\scripts\levantar.ps1 serverless
   ```
2. Detenga el contenedor de la función `notificar`:
   ```powershell
   docker stop m1-fn-notificar
   ```
3. Reserve una cita desde `http://localhost:4200/citas/nueva` o por consola:
   **Resultado observado:** La función `reservarCita` invoca a `notificar` en un hilo virtual esperando como máximo 300 ms; al fallar, responde inmediatamente **`201 Created`** con **`"notificacion": "PENDIENTE"`**.

---

### Experimento 2 (S2): Simulación de Partición de Red — `CP` vs. `AP`

1. Con `CONSISTENCIA_MODO=CP` en `m1-estilos/.env`, levante el monolito:
   ```powershell
   .\scripts\levantar.ps1 monolito
   ```
2. Desconecte al monolito de la red de datos (`m1_datos`):
   ```powershell
   .\scripts\simular-particion.ps1
   ```
3. Intente consultar disponibilidad en `http://localhost:4200/citas/nueva` o mediante `GET http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-18`:
   - **En modo `CP`:** Recibirá HTTP **`503 Service Unavailable`** (`base-de-datos-no-disponible`), que el frontend presenta en el banner de error (`ProblemDetail`).
4. Restaure la red:
   ```powershell
   .\scripts\restaurar-red.ps1
   ```
5. Ahora cambie `CONSISTENCIA_MODO=AP` en `m1-estilos/.env` y vuelva a ejecutar `.\scripts\levantar.ps1 monolito`.
6. Consulte una vez la disponibilidad de Madrid (`MAD`) con PostgreSQL conectado: verá la etiqueta verde **`Base de datos`** (`origen: BASE_DE_DATOS`).
7. Ejecute nuevamente `.\scripts\simular-particion.ps1` y refresque la disponibilidad en el navegador:
   - **En modo `AP`:** La consulta responde **`200 OK`** desde la caché Caffeine en memoria y el frontend muestra la etiqueta amarilla **`Caché – actualizado hace N s`** (`origen: CACHE`).
   - Si intenta confirmar la **reserva** (`POST /api/citas`) durante la partición, seguirá respondiendo **`503`**, porque la reserva es siempre `CP`.
8. Restaure la red con `.\scripts\restaurar-red.ps1` y deje `CONSISTENCIA_MODO=CP` en `.env`.

---

### Experimento 3 (S2): Nueva Estrategia de Tarifa `REGISTRO_CONSULAR` (Principio OCP)

1. Con el monolito en ejecución (`.\scripts\levantar.ps1 monolito`), consulte la tarifa de `REGISTRO_CONSULAR`:
   ```powershell
   curl.exe -i "http://localhost:8081/api/tarifas?tipoTramite=REGISTRO_CONSULAR&pais=ES"
   ```
   **En `main` responde error `422 Unprocessable Entity`** (`tarifa-no-disponible`), porque solo están registradas `LegalizacionTarifaStrategy` y `ApostillaTarifaStrategy`.
2. Para demostrar el principio **Abierto/Cerrado (OCP)** en clase, cree una nueva clase `RegistroConsularTarifaStrategy.java` en `monolito-modular/src/main/java/pe/utec/consular/monolito/tramites/domain/tarifa/` anotada con `@Component` que implemente `TarifaStrategy` para `TipoTramite.REGISTRO_CONSULAR`.
3. Verifique con `git status`: habrá **un único archivo nuevo** y **cero archivos modificados** (`CalculadoraDeTarifas` descubre automáticamente la nueva estrategia por inyección de `List<TarifaStrategy>`).
4. *(Importante: elimine el archivo de prueba al terminar la demostración para mantener `main` limpio).*

---

### Experimento 4 (S2): Romper la Regla de ArchUnit en `monolito` y en `clean`

1. Abra `monolito-modular/src/main/java/pe/utec/consular/monolito/citas/domain/Cita.java` (o `comparativa-clean/.../citas/entities/Cita.java`).
2. Añada una importación prohibida hacia infraestructura, por ejemplo:
   ```java
   import jakarta.persistence.Entity;
   ```
3. Ejecute las pruebas de arquitectura:
   ```powershell
   mvn test -Dtest=ReglasDeArquitecturaTest -pl monolito-modular
   mvn test -Dtest=ReglasDeArquitecturaTest -pl comparativa-clean
   ```
4. Observe cómo ArchUnit detiene la compilación indicando exactamente qué clase del dominio violó el aislamiento de capas. Revierta el cambio al finalizar.

---

## 5. Modo Híbrido en Eclipse / Spring Tools Suite

Para desarrollar o depurar paso a paso con *breakpoints* desde Eclipse sin levantar toda la JVM dentro de Docker:

1. Levante la variante deseada (por ejemplo `monolito`) para que PostgreSQL, Mailpit y el Frontend queden listos:
   ```powershell
   .\scripts\levantar.ps1 monolito
   ```
2. Detenga **únicamente** el contenedor Java que desea ejecutar desde su IDE:
   ```powershell
   docker stop m1-monolito
   ```
3. En **Spring Tools Suite / Eclipse**:
   - Importe la carpeta `m1-estilos` mediante *File → Import → Existing Maven Projects*.
   - Haga clic derecho sobre `MonolitoApplication.java` (o `CleanApplication.java`) → *Debug As → Spring Boot App*.
   - En *Run/Debug Configurations → Spring Boot → Profile*, indique **`local`** (usa `localhost:5432` y `localhost:1025`).
4. El frontend en `http://localhost:4200` se comunicará directamente con su proceso en depuración en `http://localhost:8081`.

---

## 6. Memoria y Tiempos de Arranque Medidos por Variante

Valores reales medidos en el entorno de referencia (Windows + Docker Desktop WSL 2) mediante `.\scripts\medir.ps1` y `.\scripts\carga.ps1 citas` (50 VUs durante 60 s):

| Variante | Contenedores Activos (`m1-*`) | Tiempo de Arranque (Logs JVM) | Memoria Total (`docker stats`) | Latencia `p95` (k6 50 VUs) | Caudal (`req/s`) | Tasa de Error Real |
| --- | --- | --- | --- | --- | --- | --- |
| **Monolito (`monolito`)** | `4` (`postgres`, `mailpit`, `frontend`, `monolito`) | `18.88 s` (`m1-monolito`) | **`363.44 MiB`** | `69.73 ms` | `375.33 req/s` | `0.19 %` |
| **Clean (`clean`)** | `4` (`postgres`, `mailpit`, `frontend`, `clean`) | `11.55 s` (`m1-clean`) | **`358.20 MiB`** | — | — | — |
| **Microservicios (`microservicios`)** | `5` (`postgres`, `mailpit`, `frontend`, `citas`, `notificaciones`) | `12.63 s` (`m1-citas`)<br>`10.58 s` (`m1-notificaciones`) | **`639.40 MiB`** | `136.40 ms` | `346.83 req/s` | `0.00 %` |
| **Serverless (`serverless`)** | `7` (`postgres`, `mailpit`, `frontend`, `fn-consultas`, `fn-reservar`, `fn-notificar`, `enrutador`) | `14002 ms` (`m1-fn-consultas`)<br>`10293 ms` (`m1-fn-reservar`)<br>`13353 ms` (`m1-fn-notificar`) | **`896.70 MiB`** | `182.11 ms` | `278.09 req/s` | `0.03 %` |

> Todas las variantes operan ampliamente por debajo del presupuesto máximo de **6 GB de RAM** establecido en la Regla 03 (consumiendo menos de **0.9 GB** incluso en el perfil de 7 contenedores).

---

## 7. Solución de Problemas Comunes en Windows

1. **`Docker Desktop no está en ejecución: ábralo y espere 'Engine running'`**:
   - Inicie la aplicación **Docker Desktop** desde el menú Inicio de Windows y espere a que el indicador inferior izquierdo muestre *Engine running* antes de ejecutar `.\scripts\levantar.ps1`.
2. **Puerto `8081` o `4200` ocupado por otro módulo (`m2-citas`, `m2-gateway`, etc.)**:
   - El script `levantar.ps1` detectará el contenedor en conflicto y le indicará el comando exacto para detenerlo:
     ```powershell
     ..\m2-integracion\scripts\detener.ps1
     ```
3. **Errores de esquema o datos residuales de ejecuciones anteriores**:
   - Si interrumpió una ejecución previa o desea reiniciar las tablas y secuencias desde cero, ejecute:
     ```powershell
     .\scripts\limpiar.ps1 -Forzar
     ```
4. **Fin de línea (`CRLF` vs. `LF`) en scripts `.sh`**:
   - El archivo `.gitattributes` en la raíz del repositorio fuerza `eol=lf` para `*.sh` y `eol=crlf` para `*.ps1`. Si clonó el repositorio con una configuración global restrictiva de Git, ejecute `git add --renormalize .` o verifique que `core.autocrlf` no altere los scripts `.sh`.
5. **Rendimiento de WSL 2 y memoria en Windows**:
   - Si nota lentitud tras varias horas de uso de contenedores en Windows, puede liberar la caché de Docker y reiniciar con `.\scripts\limpiar.ps1 -Forzar`.

