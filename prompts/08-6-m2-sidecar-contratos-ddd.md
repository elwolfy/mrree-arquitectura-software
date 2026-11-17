# Prompt 08-6 — M2: sidecar, contratos, DDD y README

> Rama `main`. Referencias: contrato (secciones 1.1, 6, 6.1), reglas 01–05. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Objetivo didáctico

Cerrar el módulo con el patrón Sidecar (2.2), la comparación REST vs. gRPC (2.3) y los artefactos de DDD estratégico (2.4) que se usan en el Event Storming de S3.

## Alcance

1. **Sidecar** (`m2-integracion/envoy/envoy.yaml`, perfil de compose `sidecar`, contenedor `m2-envoy`):
   - Envoy escucha en `10000` y reenvía a `tramites-service:8082` con 3 reintentos (`5xx`, `reset`) y timeout de 2 s; administración en `9901`.
   - Con el perfil activo, el gateway enruta `/api/tramites/**` a `envoy:10000` (propiedad `rutas.tramites.uri` sobrescrita en `docker-compose.sidecar.yml` o por variable).
   - Instrucciones en el README: `docker compose --profile sidecar up -d` con `FALLAS_INTERMITENTES_PORCENTAJE=50` y lectura de `localhost:9901/stats` (contadores `upstream_rq_retry`).
2. **REST vs. gRPC**: `scripts/grpc-consultar.ps1` y `.sh` ejecutan `fullstorydev/grpcurl` en contenedor contra `m2-tramites:9082` (red de compose), muestran la respuesta y comparan su tamaño en bytes con la de `GET /api/tramites/{id}`.
3. **Contratos**:
   - `docs/contratos/asyncapi.yaml` (AsyncAPI 3) con `cita.reservada` y `tramite.registrado`, cabeceras y colas del contrato 6.1. **Sin `tramite.observado`**: su documentación es la tarea conceptual de A2.
   - `docs/pruebas/colecciones/m2.postman_collection.json` (y carpeta Bruno equivalente): validar identidad con los tres DNI, iniciar trámite por BFF, consultar trámite, tarifas, inicio del ciudadano.
4. **DDD**:
   - `docs/ddd/lenguaje-ubicuo.md`: tabla del contrato 1.1 con columna de bounded context (Cupo, Cita, Solicitud, Trámite, Observación, Orden de pago, Ciudadano, Aviso).
   - `docs/ddd/context-map.md`: diagrama Mermaid con Citas, Trámites, Pagos, Identidad, Notificaciones y RENIEC·PIDE (externo); relaciones Citas → Trámites (proveedor), Trámites – Pagos (cliente-proveedor), Identidad con ACL frente a RENIEC, Trámites → Identidad (lenguaje publicado), Notificaciones conformista con los eventos; nota sobre el kernel compartido evitado. Sección final "Para el Event Storming de S3" con la convención de colores de notas.
5. **README de `m2-integracion`**: diagrama Mermaid de contenedores, tabla de puertos, experimentos de S3 en el orden del laboratorio (levantar; seguir `X-Correlation-Id`; tres DNI; detener notificaciones y observar la cola; REST vs. gRPC; sidecar), modo híbrido (detener un contenedor y ejecutarlo desde Eclipse con perfil `local`) y memoria medida con `docker stats`.

## Criterios de aceptación

- Con perfil `sidecar` y fallas al 50 %, 20 consultas seguidas a `localhost:8080/api/tramites/{id}` responden `200` y `9901/stats` muestra reintentos.
- `scripts/grpc-consultar.ps1 <id>` imprime la respuesta gRPC y la comparación de tamaños.
- `asyncapi.yaml` valida con `asyncapi validate` (CLI en contenedor).

## Commit

`feat(m2): sidecar Envoy, contratos AsyncAPI y artefactos DDD`
