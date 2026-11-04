# AGENTS.md — mrree-arquitectura-software

Repositorio didáctico del curso **Arquitectura de Software y Diseño de Soluciones** (UTEC Posgrado – MRREE). Contiene una aplicación de referencia completa (backend Java/Spring Boot + frontend Angular) que los participantes clonan, ejecutan con Docker Desktop, estudian en Eclipse y amplían en asignaciones.

## Fuente de verdad

- `docs/especificacion/contrato-global.md` define nombres, puertos, APIs, eventos, esquemas y datos semilla. **Léelo antes de cualquier tarea.** Si una instrucción contradice el contrato, detente y pregunta.
- Las reglas detalladas están en `.agents/rules/`.

## Estructura

```text
m1-estilos/          M1: monolito-modular, comparativa-clean, microservicios, serverless
m2-integracion/      M2: gateway, bff-web, citas, tramites, notificaciones, identidad, pide-simulador, envoy
m3-datos/            M3: + pagos, emision, saga, outbox, event sourcing, CQRS, redis
m4-observabilidad/   M4: + OpenTelemetry, Grafana LGTM, Structurizr
frontend/            Angular + PrimeNG, compartido por todos los módulos
docs/                especificacion, instalacion, asignaciones, adr, c4, matrices, pruebas
```

## Comandos

| Acción | Comando |
| --- | --- |
| Levantar un módulo | `cd <modulo> && docker compose up -d --build` (M1 requiere `--profile monolito|microservicios|serverless|clean`) |
| Detener y limpiar | `docker compose down -v` |
| Pruebas Java | `docker run --rm -v "$PWD":/w -w /w maven:3.9-eclipse-temurin-21 mvn -q verify` (desde la carpeta del módulo) |
| Pruebas frontend | `docker run --rm -v "$PWD/frontend":/w -w /w node:22-alpine sh -c "npm ci && npm test -- --watch=false"` |
| Carga (M1) | `docker compose --profile carga run --rm k6` |

## Reglas no negociables

1. `main` = aplicación completa **sin** las funcionalidades de A1–A4. Esas van solo en `solucion/aN`.
2. Todo se construye y ejecuta con Docker (build multi-etapa). No asumir herramientas instaladas en el host.
3. Idioma: español para código de dominio, comentarios, mensajes, logs de negocio y documentación.
4. Arquitectura hexagonal en todos los servicios (salvo `comparativa-clean`, que usa Clean Architecture). Las reglas se verifican con ArchUnit; nunca desactivar una prueba de arquitectura para que pase el build.
5. Código didáctico: cada clase que ilustra un concepto del curso lleva Javadoc con `Concepto:` y `Sesión:`.
6. Datos ficticios de laboratorio; nunca usar datos reales de personas ni credenciales reales.
7. Antes de terminar una tarea: compilar, ejecutar pruebas, levantar los contenedores y verificar los criterios de aceptación del prompt. Reportar evidencia en el Walkthrough.
