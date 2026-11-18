# Módulo 3 (`m3-datos`) — Gestión de Datos, Modelos Políglotas y Consistencia Distribuida

El **Módulo 3 (`m3-datos`)** evoluciona la plataforma consular desde la arquitectura de microservicios de M2 hacia el manejo avanzado de datos distribuidos (Sesiones **S5** y **S6**):

1. **Patrón *Database-per-Service*** sobre un motor PostgreSQL compartido mediante aislamiento estricto de esquemas y roles (`citas_app`, `tramites_app`, `pagos_app`, `emision_app`, `notificaciones_app`, `identidad_app`).
2. **Modelo Documental (`JSONB` + Índice `GIN`)** en `tramites.expediente(tramite_id, metadatos)` para almacenar atributos variables según el tipo de trámite (`LEGALIZACION`, `APOSTILLA`, `REGISTRO_CONSULAR`) y consultarlos con el operador de contención `@>`.
3. **Redis (`m3-redis`, `:6379`)** para caché *Cache-Aside* e idempotencia distribuida.
4. **CQRS, Event Sourcing, Saga Orquestada y Transactional Outbox** (que se completan progresivamente en los siguientes bloques del módulo).

---

## 1. Requisitos previos y verificación de puertos

Antes de levantar `m3-datos`, asegúrese de que **Docker Desktop** esté activo y que no haya contenedores en ejecución de `m1-estilos` o `m2-integracion` ocupando los puertos `8080`, `8081`, `5432` o `4200`.

### Levantar el entorno completo

**PowerShell (Windows):**
```powershell
cd m3-datos
.\scripts\levantar.ps1
```

**Bash (Linux / macOS / Git Bash):**
```bash
cd m3-datos
./scripts/levantar.sh
```

---

## 2. Patrón *Database-per-Service* (`postgres/initdb/01-esquemas.sql`)

Cada microservicio posee su propio esquema en la base `consular` y se conecta exclusivamente con su usuario `<esquema>_app` / `<esquema>_pwd`:

| Esquema | Usuario PostgreSQL | Microservicio propietario |
|---|---|---|
| `citas` | `citas_app` | `citas-service` (`:8081`) |
| `tramites` | `tramites_app` | `tramites-service` (`:8082` / gRPC `:9082`) |
| `pagos` | `pagos_app` | `pagos-service` (`:8083`) |
| `notificaciones` | `notificaciones_app` | `notificaciones-service` (`:8084`) |
| `identidad` | `identidad_app` | `identidad-service` (`:8086`) |
| `emision` | `emision_app` | `emision-service` (`:8087`) |

Para inspeccionar los esquemas y el índice GIN desde `psql`:

**PowerShell:**
```powershell
cd m3-datos
.\scripts\psql.ps1 -c "\dn"
.\scripts\psql.ps1 -c "\d tramites.expediente"
```

**Bash:**
```bash
cd m3-datos
./scripts/psql.sh -c "\dn"
./scripts/psql.sh -c "\d tramites.expediente"
```

---

## 3. Expediente Documental (`tramites.expediente`) y Búsqueda JSONB (`@>`)

Al registrar un trámite (`POST /api/tramites`), `tramites-service` almacena en `tramites.expediente(tramite_id, metadatos)` un documento `JSONB` cuya estructura varía según el subtipo:

- `LEGALIZACION`: `{"folios": 3, "idioma": "ES"}`
- `APOSTILLA`: `{"paisDestino": "ES", "idioma": "ES"}`
- `REGISTRO_CONSULAR`: `{"paisDestino": "ES", "idioma": "ES"}`

### Consulta REST mediante el operador `@>`

```bash
curl -s "http://localhost:8080/api/tramites/buscar?pais=ES"
```

### Consultas SQL directas en `psql`

Con la tabla relacional `tramites.tramite` (etapa 09-1):
```sql
SELECT t.codigo, e.metadatos->>'idioma' AS idioma
FROM tramites.expediente e
JOIN tramites.tramite t ON t.id = e.tramite_id
WHERE e.metadatos @> '{"paisDestino":"ES"}';
```

Con la proyección de lectura CQRS `tramites.vista_tramite` (a partir de la etapa 09-2):
```sql
SELECT v.codigo, e.metadatos->>'idioma' AS idioma
FROM tramites.expediente e
JOIN tramites.vista_tramite v ON v.id = e.tramite_id
WHERE e.metadatos @> '{"paisDestino":"ES"}';
```

---

## 4. Detener y limpiar el entorno

**PowerShell:**
```powershell
cd m3-datos
.\scripts\detener.ps1
.\scripts\limpiar.ps1 -Forzar
```

**Bash:**
```bash
cd m3-datos
./scripts/detener.sh
./scripts/limpiar.sh --forzar
```

