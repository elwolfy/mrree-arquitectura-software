# Prompt 08 — M2: Integración, comunicación y DDD (índice)

> **Este prompt ya no se ejecuta de una sola vez.** El Módulo 2 se genera en siete pasos, uno por conversación, en este orden. Cada paso deja el repositorio compilando y con commit propio, para revisar el *Implementation Plan* y el *Walkthrough* en partes manejables.

| Paso | Archivo | Resultado | Contenido del curso |
| --- | --- | --- | --- |
| 08-1 | `08-1-m2-base-y-citas.md` | Esqueleto de `m2-integracion`, compose base, RabbitMQ, `citas-service` publicando `cita.reservada` | 2.3 |
| 08-2 | `08-2-m2-tramites.md` | `tramites-service`: Factory Method, Builder, `tramite.registrado`, servidor gRPC | 2.1, 2.3 |
| 08-3 | `08-3-m2-identidad-pide.md` | `identidad-service` (Adapter + ACL) y `pide-simulador` (WireMock) | 2.1, 2.3, 2.4 |
| 08-4 | `08-4-m2-notificaciones.md` | `notificaciones-service`: consumidores idempotentes, DLQ, Mailpit, avisos | 2.1 (Observer), 2.3 |
| 08-5 | `08-5-m2-pagos-bff-gateway.md` | `pagos-service` (Strategy), `bff-web` (Facade, agregación), `gateway` | 2.1, 2.2 |
| 08-6 | `08-6-m2-sidecar-contratos-ddd.md` | Envoy (perfil `sidecar`), gRPC script, AsyncAPI, Postman, `docs/ddd`, README | 2.2, 2.3, 2.4 |
| 08-7 | `08-7-m2-verificacion-y-a2.md` | Verificación integral de M2, frontend en modo m2, guía y solución de A2 | Todos |

**Regla común a los siete pasos:** antes de crear o modificar archivos, genera un *Implementation Plan* y espera mi aprobación. Respeta el contrato global (`docs/especificacion/contrato-global.md`, secciones 1, 3, 6 y 6.1) y las reglas 01–06. Las rutas de clases que se indican en cada paso aparecen tal cual en la presentación y el material de estudio del Módulo 2: no las renombres.
