# Plataforma Digital de Servicios Consulares y de Legalización

Repositorio didáctico del curso **Arquitectura de Software y Diseño de Soluciones** (UTEC Posgrado – MRREE). Contiene una aplicación de referencia completa (backend Java 21 / Spring Boot + frontend Angular con PrimeNG) que ilustra de manera práctica y evolutiva los conceptos fundamentales del diseño de soluciones empresariales.

> **Aviso de datos de laboratorio:**  
> Todos los datos (oficinas consulares, ciudadanos, DNI, trámites, tarifas y constancias) son **estrictamente ficticios y con fines didácticos**. No corresponden ni representan información real del Ministerio de Relaciones Exteriores del Perú (MRREE) ni de persona alguna.

---

## Módulos del curso

Cada módulo es autónomo, se ubica en su propia carpeta y dispone de su propio archivo `docker-compose.yml`.

| Carpeta | Sesiones | Conceptos ilustrados | Cómo levantar |
| :--- | :--- | :--- | :--- |
| [`m1-estilos/`](m1-estilos/) | S1 – S2 | Monolito modular, Clean Architecture vs. Hexagonal, Microservicios, Serverless (FaaS), Teorema CAP/PACELC, Patrón Strategy | `cd m1-estilos && docker compose --profile monolito up -d --build`<br>*(perfiles: `monolito`, `clean`, `microservicios`, `serverless`)* |
| [`m2-integracion/`](m2-integracion/) | S3 | API Gateway (Spring Cloud Gateway), BFF Web, Comunicación gRPC vs. REST, Mensajería asíncrona con RabbitMQ, Patrón Sidecar (Envoy) | `cd m2-integracion && docker compose up -d --build`<br>*(perfil opcional: `sidecar`)* |
| [`m3-datos/`](m3-datos/) | S4 – S5 | CQRS, Event Sourcing, Patrón Saga orquestada, Patrón Outbox transaccional, Idempotencia, Caché Clave-Valor (Redis) | `cd m3-datos && docker compose up -d --build` |
| [`m4-observabilidad/`](m4-observabilidad/) | S6 | OpenTelemetry (agente OTLP), Grafana LGTM (Logs, Metrics, Traces), Métricas Micrometer, Arquitectura C4 en Structurizr Lite, Deuda técnica | `cd m4-observabilidad && docker compose up -d --build`<br>*(perfil opcional: `calidad`)* |
| [`frontend/`](frontend/) | S1 – S6 | Angular standalone, PrimeNG, consumo de API/BFF, visibilidad didáctica de consistencia eventual y atributos de calidad | Incluido en el docker compose de cada módulo en el puerto `4200` |
| [`docs/`](docs/) | S1 – S6 | Especificación funcional y técnica, arquitectura C4, registros de decisiones arquitectónicas (ADR), matrices comparativas y guías de laboratorio | N/A (documentación Markdown y DSL) |

---

## Requisitos del equipo

Para ejecutar y trabajar con los proyectos del curso se requiere:

- **Sistema Operativo:** Windows 10/11 de 64 bits (o macOS / Linux).
- **Virtualización:** Docker Desktop con backend WSL 2 activo (o Rancher Desktop / Podman Desktop con soporte Docker Compose).
- **Recursos recomendados:** 8 GB de memoria RAM como mínimo (16 GB recomendado), al menos 15 GB de espacio libre en disco.
- **Herramientas de desarrollo:**
  - Git
  - JDK 21 (Eclipse Temurin)
  - Spring Tools 4 for Eclipse (STS) o IDE de su preferencia (IntelliJ IDEA / VS Code)
- *Nota:* No es indispensable tener Apache Maven ni Node.js/npm instalados localmente en el host; todas las compilaciones, empaquetados y ejecuciones se realizan mediante contenedores Docker con build multi-etapa.

Para instrucciones paso a paso sobre cómo preparar su entorno y configurar el modo de ejecución híbrido (Spring Boot en Eclipse + servicios en Docker), consulte la guía en [docs/instalacion/](docs/instalacion/).

---

## Modelo de ramas y asignaciones

El repositorio gestiona el ciclo de vida del código mediante las siguientes ramas y etiquetas:

| Rama / Tag | Propósito | Responsable |
| :--- | :--- | :--- |
| `main` | Aplicación base de referencia completa. **Protegida**; no contiene las soluciones de las asignaciones A1–A4. | Docente (vía Pull Request) |
| `solucion/a1` … `solucion/a4` | Ramas con la solución oficial de cada asignación práctica (A1 a A4). Cada rama parte directamente de `main`. | Docente |
| `demo/strangler` | Demostración práctica del patrón Strangler Fig (Sesión 6). | Docente |
| Tags `s1` … `s6` | Puntos de partida de referencia para cada una de las sesiones sincrónicas del curso. | Docente |
| `aN-<usuario>` | Rama de trabajo de cada estudiante creada dentro de su propio **fork**. | Participante |

### Flujo de trabajo del participante
1. Realice un **Fork** de este repositorio hacia su cuenta de GitHub.
2. Clone su fork localmente y configure el upstream remoto.
3. Cree una rama específica para cada asignación (por ejemplo: `a1-jperez`).
4. Desarrolle la asignación, ejecute las pruebas unitarias y de arquitectura (`mvn test` / `mvn verify`), y verifique el comportamiento en Docker.
5. Registre sus entregas según lo indicado en cada guía de `docs/asignaciones/`.

---

## Fuente de verdad

El contrato que rige nombres de esquemas, endpoints REST, contratos gRPC, eventos de dominio, puertos y datos semilla está definido en:
- [`docs/especificacion/contrato-global.md`](docs/especificacion/contrato-global.md)
