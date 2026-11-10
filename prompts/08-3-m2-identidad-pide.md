# Prompt 08-3 — M2: `identidad-service` (Adapter + ACL) y `pide-simulador`

> Rama `main`. Referencias: contrato (secciones 1.1, 6), reglas 01–04. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Objetivo didáctico

Mostrar la integración con un sistema que no controlamos: la consulta de identidad de RENIEC publicada en la Plataforma de Interoperabilidad del Estado (PIDE). El mismo código ilustra el patrón Adapter (2.1) y la capa anticorrupción del context map (2.4). **La PIDE se simula; no se llama a ningún servicio real.**

## Alcance

1. **`pide-simulador`** (`m2-integracion/pide-simulador`, contenedor `m2-pide`, puerto 8089) con imagen oficial de WireMock y carpeta `mappings/`:
   - Un endpoint con forma y nomenclatura **deliberadamente ajenas** al dominio (por ejemplo `POST /ws/reniec/consultarDatos` con respuesta `{ "coResultado": "0000", "deResultado": "...", "datosPersona": { "prenombres": "...", "apPrimer": "...", "apSegundo": "...", "estadoCivil": "...", "inFallecido": "N" } }`).
   - DNI `40000001`–`40000050`: válidos, con nombres ficticios generados por plantilla de respuesta.
   - DNI `49999999`: persona fallecida (`inFallecido: "S"`).
   - DNI `48888888`: no encontrado (`coResultado` de error).
   - Retardo fijo de 300 ms en todas las respuestas.
   - Escenario WireMock que, al activarse por la API de administración, responde `503` (para probar resiliencia).
2. **`identidad-service`** (8086, `pe.utec.consular.identidad`, hexagonal):
   - `POST /api/identidad/validaciones {dni}` → `Ciudadano` propio (`dni`, `nombres`, `apellidos`, `vigente`) o `ProblemDetail` con mensaje claro para fallecido y no encontrado.
   - Puerto de salida `application/port/out/ValidarIdentidadPort.java`.
   - **Adapter / ACL**: `adapter/out/pide/ReniecPideAdapter.java` implementa el puerto, llama al simulador con `RestClient`, y traduce el modelo externo al modelo propio mediante `adapter/out/pide/ReniecRespuesta.java` (DTO externo) y un traductor. Ninguna clase fuera de `adapter/out/pide` conoce nombres como `coResultado` o `prenombres`. Javadoc de la clase que explique Adapter (GoF) y capa anticorrupción (DDD).
   - Resiliencia con Resilience4j: tiempo máximo 2 s, 2 reintentos y *circuit breaker*; si el circuito está abierto, responde `503` con `ProblemDetail` "Servicio de identidad no disponible".
   - Propiedad `pide.url` (por defecto `http://pide-simulador:8080`, en perfil `local` `http://localhost:8089`).
3. Agregar ambos al compose.

## Pruebas

- Integración con WireMock embebido (o Testcontainers WireMock): los tres casos de DNI y el `503`.
- ArchUnit: los tipos de `adapter.out.pide` no son usados por `domain` ni `application`.

## Criterios de aceptación

- `POST localhost:8086/api/identidad/validaciones` con `40000001`, `49999999` y `48888888` devuelve, respectivamente, `200` con `Ciudadano`, `422` "identidad no vigente" y `404` "DNI no encontrado".
- `git grep -n "coResultado" m2-integracion/identidad-service/src/main` solo encuentra archivos en `adapter/out/pide`.

## Commit

`feat(m2): identidad-service con ACL hacia la PIDE simulada`
