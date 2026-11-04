# Prompt 05 — M1: Variante serverless (emulada localmente)

> Rama `main`. Referencias: contrato (secciones 3–5, 4.3), reglas 01–04.

## Objetivo didáctico

Mostrar el modelo de **funciones**: una función por operación, sin estado, invocada por eventos HTTP, desplegada y escalada por separado. En local se emula con Spring Cloud Function: la **misma imagen** se ejecuta varias veces, cada contenedor con una sola función activa (`spring.cloud.function.definition`). Un enrutador nginx publica la API del contrato en `:8081`, como lo haría un API Gateway del proveedor de nube.

Debe quedar claro en el README que esto **emula** el modelo; el experto muestra en clase la misma función en un proveedor de nube (AWS Lambda o Azure Functions) si hay cuenta de laboratorio. Incluir en el proyecto el adaptador necesario documentado (por ejemplo, perfil Maven `aws` con `spring-cloud-function-adapter-aws`) **sin** que sea requisito para ejecutar en local.

## Alcance

```text
m1-estilos/serverless/
├── funciones-citas/        artifactId consular-m1-funciones-citas (una sola aplicación con varias funciones)
└── enrutador/              nginx.conf + Dockerfile (nginx:alpine)
```

### Funciones (beans `java.util.function`)

| Función | Tipo | Ruta pública (enrutador) | Contenedor |
| --- | --- | --- | --- |
| `listarOficinas` | `Supplier<List<OficinaDto>>` | `GET /api/oficinas` | `m1-fn-consultas` |
| `consultarDisponibilidad` | `Function<Message<?>, DisponibilidadDto>` | `GET /api/oficinas/{id}/disponibilidad` | `m1-fn-consultas` |
| `consultarCita` | `Function<…>` | `GET /api/citas/{id}`, `GET /api/citas?dni=` | `m1-fn-consultas` |
| `reservarCita` | `Function<RegistrarCitaRequest, CitaDto>` | `POST /api/citas` | `m1-fn-reservar` |
| `cancelarCita` | `Consumer`/`Function` | `DELETE /api/citas/{id}` | `m1-fn-reservar` |
| `notificar` | `Consumer<AvisoRequest>` | (interna) `POST /notificar` | `m1-fn-notificar` |
| `infoSistema` | `Supplier<InfoSistemaDto>` | `GET /api/sistema/info` | `m1-fn-consultas` |

- Tres contenedores de la misma imagen: `m1-fn-consultas` (definition `listarOficinas;consultarDisponibilidad;consultarCita;infoSistema` con enrutamiento por cabecera/ruta), `m1-fn-reservar` (`reservarCita;cancelarCita`), `m1-fn-notificar` (`notificar`). Documentar que en un proveedor real cada función sería una unidad independiente; aquí se agrupan para no exceder la memoria del equipo del participante.
- `reservarCita` invoca `notificar` de forma **asíncrona** (`RestClient` en un hilo virtual, sin esperar respuesta) → la reserva siempre responde con `notificacion: PENDIENTE` si la función de notificación falla o tarda; si responde en < 300 ms, `ENVIADA`.
- Las funciones no guardan estado en memoria entre invocaciones: todo en PostgreSQL (esquemas `citas` y `notificaciones`).
- `infoSistema` → `variante: SERVERLESS`.
- Registrar en el log el tiempo de arranque del contenedor ("arranque en frío") de forma visible: `Función lista en N ms`.
- La lógica de dominio puede reutilizar el diseño de citas del monolito (copia), pero organizada por función: `pe.utec.consular.serverless.funciones` (beans) + `dominio` + `infraestructura`.

### Enrutador

`nginx.conf` que mapea rutas y métodos a los contenedores (`location` + `if ($request_method …)` o `map`), agrega cabecera `X-Funcion-Invocada` en la respuesta y maneja CORS para `localhost:4200`.

### Pruebas

- Unitarias de cada función (sin Spring cuando sea posible).
- Integración de `reservarCita` con Testcontainers.

## Criterios de aceptación

- `mvn -B verify` pasa.
- Con un compose temporal (postgres + mailpit + 3 contenedores de función + enrutador), las peticiones curl del README del monolito funcionan en `:8081` (salvo `/api/tarifas`, documentado como no disponible en esta variante).
- `docker stop m1-fn-notificar` → la reserva responde `201` con `PENDIENTE`.
- El log muestra el tiempo de arranque de cada función.

## Commit

`feat(m1): variante serverless`
