# Lineamientos de Diseño y Gobernanza de APIs REST — Plataforma Consular (Borrador v0.1)

- **Versión:** `0.1` (Borrador de trabajo para completar en la Sesión 6)
- **Marco Normativo:** Alineado con el **Decreto Legislativo N.° 1412** (*Ley de Gobierno Digital*), su Reglamento (D.S. N.° 029-2021-PCM) y los estándares de interoperabilidad de la **Plataforma Nacional de Interoperabilidad del Estado (PIDE)** gestionada por la Secretaría de Gobierno y Transformación Digital (SGTD – PCM).
- **Validación Automatizada:** Reglas ejecutables en [docs/estandares/.spectral.yaml](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/docs/estandares/.spectral.yaml) mediante el script `m4-observabilidad/scripts/validar-apis.ps1`.

---

## 1. Nombres de Recursos y Rutas (URIs)

1. **Sustantivos en plural y minúsculas (`kebab-case`)**:
   - Los recursos HTTP representan entidades o conceptos del lenguaje ubicuo consular en sustantivos plurales (`/api/citas`, `/api/oficinas`, `/api/tramites`, `/api/pagos`, `/api/tarifas`, `/api/avisos`, `/api/identidad/validaciones`).
   - Los segmentos de ruta con más de una palabra se separan con guiones (`kebab-case`, p. ej. `/vista-tramite`), **nunca** `camelCase` ni `snake_case`.
   - Expresión regular verificada automáticamente por Spectral (`rutas-en-kebab-case`, severidad `error`):
     ```regex
     ^(/[a-z0-9-{}]+)+$
     ```
2. **Acciones de negocio como sub-recursos sustantivados**:
   - En lugar de verbos en la URL (`/anularPago` o `/reprogramarCita`), las transiciones de negocio se modelan como sub-recursos sustantivados:
     - `POST /api/pagos/{id}/anulacion`
     - `PUT /api/citas/{id}/reprogramacion`
     - `PUT /api/tramites/{id}/observacion`

---

## 2. Versionamiento y Evolución de Contratos

1. **Evolución compatible hacia atrás (*Backward Compatibility*)**:
   - No se deben eliminar ni renombrar campos existentes en los DTOs de respuesta sin un periodo de deprecación documentado.
2. **Estrategia de versionamiento**:
   - **APIs gRPC internas** (`tramites.proto`): paquete explícito con versión mayor (`package consular.tramites.v1;`).
   - **APIs REST públicas**: evolución aditiva compatible dentro de `/api/**` y migración gradual de consumidores heredados mediante el patrón **Strangler Fig** en el API Gateway (por ejemplo, enrutamiento condicional con cabecera `X-Version: nueva`).

---

## 3. Estándar de Errores HTTP (RFC 9457 — Problem Details)

Todas las respuestas de error (`4xx` y `5xx`) devuelven `application/problem+json` siguiendo **RFC 9457** (`org.springframework.http.ProblemDetail`), sin exponer trazas de pila (*stack traces*) ni detalles internos de tablas SQL:

```json
{
  "type": "https://consular.lab/errores/cita-duplicada",
  "title": "Cita duplicada para el ciudadano",
  "status": 409,
  "detail": "El ciudadano con DNI 40000001 ya posee una cita RESERVADA en la oficina MAD para la fecha 2026-11-30",
  "instance": "/api/citas",
  "codigo": "cita-duplicada"
}
```

| Código HTTP | Propiedad `codigo` | Uso en la Plataforma Consular |
| --- | --- | --- |
| `400 Bad Request` | `validacion` | Error de validación sintáctica (Jakarta Validation) o falta de `Idempotency-Key`. |
| `404 Not Found` | `no-encontrado` | Recurso inexistente o DNI no registrado en el padrón PIDE/RENIEC. |
| `409 Conflict` | `cita-duplicada`, `cupo-sin-capacidad`, `conflicto-estado` | Violación de invariante de concurrencia o transición inválida de estado. |
| `422 Unprocessable Entity` | `identidad-no-valida`, `regla-negocio` | Inconsistencia semántica del dominio (p. ej. ciudadano fallecido en RENIEC o folios inválidos). |
| `429 Too Many Requests` | `limite-tasa-excedido` | Cuota de peticiones superada en el API Gateway (`/api/identidad/validaciones`). |
| `503 Service Unavailable` | `base-de-datos-no-disponible`, `servicio-externo-no-disponible` | Falla de disponibilidad en modo CP o apertura de Circuit Breaker hacia la PIDE. |

---

## 4. Paginación y Consultas sobre Grandes Volúmenes

1. **Prohibición de listados sin acotar**:
   - Toda consulta de colección debe filtrarse por clave indexada (`?dni=XXXXXXXX`) o paginarse.
2. **Paginación profunda (*Keyset Pagination*)**:
   - Para listados históricos de alto volumen (como `tramites.vista_tramite` con $\ge 1\,000\,000$ de registros, analizado en la Sesión 5), se prefiere paginación por cursor compuesto `(actualizado_en, tramite_id) < (:cursorFecha, :cursorId) LIMIT :tamano` en lugar de `OFFSET` lineal.

---

## 5. Seguridad, Datos Personales e Interoperabilidad PIDE

1. **Protección de Datos Personales (Ley N.° 29733 y D.L. N.° 1412)**:
   - El DNI y el correo electrónico del ciudadano se transmiten únicamente en el cuerpo de peticiones `POST` o como parámetros de consulta acotados; en los registros de aplicación (*logs JSON*), el DNI debe enmascararse (`****0001`) y nunca deben registrarse credenciales ni correos completos.
2. **Interoperabilidad con la PIDE (RENIEC)**:
   - Ningún servicio de negocio consume directamente contratos SOAP/XML o códigos propietarios de la PIDE. Toda integración pasa por `identidad-service` (`POST /api/identidad/validaciones`), que aplica una **Capa Anticorrupción (ACL)**, timeout de 2 s, Circuit Breaker y Rate Limiting en el Gateway.

---

## 6. Idempotencia y Trazabilidad

1. **Idempotencia obligatoria en operaciones financieras (`Idempotency-Key`)**:
   - `POST /api/pagos` exige la cabecera HTTP `Idempotency-Key` (UUIDv4 generado por el cliente). Los reintentos con la misma clave devuelven la misma orden de pago sin duplicar cobros.
2. **Correlación y Trazabilidad Distribuida**:
   - Todas las peticiones aceptan y propagan `X-Correlation-Id` y la cabecera W3C Trace Context (`traceparent`).

---

## 7. Reglas propuestas en clase

<!-- Sección reservada para completar en vivo durante el taller de Gobernanza Técnica de la Sesión 6 -->

