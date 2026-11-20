# Modelo C4 de la Plataforma Consular (Structurizr DSL)

> **Sesión 5 — Documentación de Arquitectura como Código (`Architecture as Code`)**
> Archivo fuente: [workspace.dsl](workspace.dsl)

---

## 1. Cómo Abrir y Explorar el Modelo C4 (`http://localhost:8090`)

El modelo C4 se visualiza de forma interactiva mediante el contenedor liviano `m4-structurizr` (`structurizr/lite:2025.11.08`), que monta en caliente la carpeta `docs/c4/`. No es necesario levantar toda la plataforma de microservicios para trabajar con los diagramas en la Sesión 5 ni en la Asignación A4:

### En Windows (PowerShell)

```powershell
cd m4-observabilidad
.\scripts\levantar-c4.ps1
```

### En Linux / macOS (Bash)

```bash
cd m4-observabilidad
./scripts/levantar-c4.sh
```

Una vez que el script indique `[OK] m4-structurizr -> healthy`, abra en su navegador:
- **URL:** `http://localhost:8090`

Al guardar cualquier cambio en `docs/c4/workspace.dsl` desde su editor o Eclipse y recargar la página en el navegador, Structurizr recompila y actualiza los diagramas instantáneamente.

---

## 2. Vistas Incluidas en `workspace.dsl`

| Clave de Vista | Nivel C4 | Descripción |
| --- | --- | --- |
| `ContextoSistema` | **C1 – Contexto del Sistema** | Personas (**Ciudadano**, **Funcionario Consular**), el sistema **Plataforma Consular** y los sistemas externos (**PIDE / RENIEC**, **Pasarela de Pagos**, **Servicio de Correo**). |
| `Contenedores` | **C2 – Contenedores** | `Frontend Ciudadano` (`:4200`), `API Gateway` (`:8080`), `BFF Web` (`:8085`), `citas-service` (`:8081`), `tramites-service` (`:8082/:9082`), `identidad-service` (`:8086`), `pagos-service` (`:8083`), `emision-service` (`:8087`), `notificaciones-service` (`:8084`), `PostgreSQL` (`:5432`, con nota de esquemas aislados por servicio) y `RabbitMQ` (`:5672`). *(Nota: Redis se incorpora como Tarea 1 de la Asignación A4).* |
| `ComponentesTramites` | **C3 – Componentes (`tramites-service`)** | Arquitectura interna de `tramites-service`: adaptadores primarios (`TramiteController`, `TramitesGrpcService`, `SagaEventosConsumer`), casos de uso, `SagaSolicitudTramite`, `ProyectorVistaTramite`, `RelayOutbox` y adaptadores secundarios. |
| `FlujoSagaSolicitudPagoEmision` | **Vista Dinámica** | Secuencia numerada (pasos 1 al 18) del flujo completo **Solicitud → Pago Idempotente → Emisión Documental** coordinado por la Saga Orquestada y el Transactional Outbox. |

---

## 3. Cómo Validar el DSL y Exportar Diagramas (PNG / SVG / PlantUML / Mermaid)

### 3.1 Validación por línea de comandos (Structurizr CLI en contenedor)

```powershell
docker run --rm -v "${PWD}/docs/c4:/usr/local/structurizr" structurizr/cli:2025.11.09 validate -workspace /usr/local/structurizr/workspace.dsl
```

### 3.2 Exportación desde la interfaz web (`http://localhost:8090`)
1. Abra la vista que desea exportar (`ContextoSistema`, `Contenedores`, `ComponentesTramites` o `FlujoSagaSolicitudPagoEmision`).
2. Haga clic en el botón **Export** (icono de descarga en la barra de herramientas del diagrama) y elija **PNG** o **SVG** para incluir la imagen en informes o ADRs.

### 3.3 Exportación a PlantUML o Mermaid por CLI

```powershell
docker run --rm -v "${PWD}/docs/c4:/usr/local/structurizr" structurizr/cli:2025.11.09 export -workspace /usr/local/structurizr/workspace.dsl -format mermaid -output /usr/local/structurizr/export
```

---

## 4. Equivalencia y Relación entre el Modelo C4 y ArchiMate

En entidades públicas como el MRREE (alineadas con el Marco de Gobierno Digital y TOGAF / ArchiMate 3.2), los diagramas C4 de arquitectura de software se mapean directamente con los elementos de las capas de **Negocio**, **Aplicación** y **Tecnología** de **ArchiMate**:

| Elemento en el Modelo C4 (`workspace.dsl`) | Concepto ArchiMate 3.2 Equivalente | Capa ArchiMate | Ejemplo en la Plataforma Consular |
| --- | --- | --- | --- |
| **Person (`person`)** | **Business Actor** (o *Stakeholder* externo) | Capa de Negocio (*Business Layer*) | `Ciudadano`, `Funcionario Consular` |
| **Software System (`softwareSystem` propio)** | **Application Collaboration** o **Application Component** de grano grueso | Capa de Aplicación (*Application Layer*) | `Plataforma Consular` |
| **External Software System (`softwareSystem` externo)** | **Application Component** externo expuesto mediante una **Application Interface** | Capa de Aplicación (*Application Layer*) | `PIDE / RENIEC`, `Pasarela de Pagos`, `Servicio de Correo` |
| **Container (`container` de aplicación / microservicio)** | **Application Component** desplegado sobre un **Node / System Software** | Capa de Aplicación + Capa de Tecnología | `API Gateway`, `BFF Web`, `tramites-service`, `pagos-service`, `emision-service` |
| **Container (`container` de datos / mensajería)** | **System Software** + **Data Object / Artifact** | Capa de Tecnología + Capa de Aplicación | `PostgreSQL (Esquemas por Servicio)`, `RabbitMQ Broker`, `Redis` (A4) |
| **Component (`component` interno de un servicio)** | **Application Function** / **Application Service** / sub-componente de un *Application Component* | Capa de Aplicación (*Application Layer*) | `SagaSolicitudTramite`, `ProyectorVistaTramite`, `RelayOutbox` |
| **Relationship (`->` con protocolo)** | **Flow Relationship** o **Serving Relationship** a través de una **Application Interface** | Transversal | `HTTP/JSON`, `gRPC`, `AMQP (:5672)`, `JDBC (:5432)` |

