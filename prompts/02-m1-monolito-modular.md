# Prompt 02 — M1: Monolito modular hexagonal

> Rama `main`. Modo Planning. Referencias: `@docs/especificacion/contrato-global.md` (secciones 1–5), reglas 01, 02, 04.

## Objetivo didáctico

Esta aplicación es la base de las sesiones S1 y S2: atributos de calidad (1.1), variante "monolito" de la comparativa de estilos (1.2), arquitectura hexagonal (1.3), CAP (1.4) y patrones Strategy/Observer/Adapter (2.1). Debe ser **ejemplar**: el participante la leerá en Eclipse línea por línea.

## Alcance

Proyecto `m1-estilos/monolito-modular` (artifactId `consular-monolito`, clase principal `pe.utec.consular.monolito.MonolitoApplication`), agregado como `<module>` en `m1-estilos/pom.xml`.

### Módulos (Spring Modulith)

```text
pe.utec.consular.monolito
├── citas/            Reserva y consulta de citas, cupos, oficinas, modo CAP
├── tramites/         En M1 solo Tarifas (patrón Strategy) — anticipa M2
├── notificaciones/   Envío de avisos (consola / correo Mailpit), simulación de falla
└── compartido/       Tipos comunes mínimos (Dni, Email como objetos de valor), manejo de errores
```

Cada módulo sigue la estructura hexagonal de la regla 01. Clases mínimas esperadas en `citas`:

| Paquete | Clases |
| --- | --- |
| `domain` | `Cita`, `Cupo`, `OficinaConsular`, `CodigoCita`, `EstadoCita`, `TipoTramite`, `CitaReservada` (evento de dominio), `ReglasDeReserva` |
| `application.port.in` | `RegistrarCitaUseCase` (+ `RegistrarCitaCommand`), `ConsultarCitaUseCase`, `CancelarCitaUseCase`, `ConsultarDisponibilidadUseCase`, `ListarOficinasUseCase` |
| `application.port.out` | `CitaRepositoryPort`, `CupoRepositoryPort`, `OficinaRepositoryPort`, `NotificacionPort`, `DisponibilidadCachePort` |
| `application.service` | `RegistrarCitaService`, `ConsultarCitaService`, `CancelarCitaService`, `ConsultarDisponibilidadService`, `ListarOficinasService`, `GeneradorDeCupos` |
| `adapter.in.web` | `CitaController`, `OficinaController`, `SistemaController`, DTO `record` del contrato, `CitaWebMapper` |
| `adapter.out.persistence` | `CitaJpaEntity`, `CupoJpaEntity`, `OficinaJpaEntity`, repositorios Spring Data, `CitaJpaAdapter`, `CupoJpaAdapter`, `OficinaJpaAdapter` |
| `adapter.out.notificaciones` | `NotificacionModuloAdapter` (llama a la API pública del módulo notificaciones) |
| `adapter.out.cache` | `DisponibilidadCaffeineAdapter` |

Nombres de clases y paquetes **exactamente** como en la tabla de equivalencias del plan (sección 7, 1.3): `adapter.in.web.CitaController`, `application.port.in.RegistrarCitaUseCase`, `application.service.RegistrarCitaService`, `domain.Cita`, `application.port.out.CitaRepositoryPort`, `adapter.out.persistence.CitaJpaAdapter`.

### Módulo `tramites` (tarifas, patrón Strategy)

- `domain.tarifa.TarifaStrategy` (interfaz: `soporta(TipoTramite)`, `calcular(String pais)`), `LegalizacionTarifaStrategy`, `ApostillaTarifaStrategy`, `CalculadoraDeTarifas` (selecciona la estrategia; lanza `TarifaNoDisponibleException` si ninguna soporta el tipo).
- Las estrategias se registran como beans y se inyectan como `List<TarifaStrategy>`: agregar una nueva estrategia **no requiere modificar** clases existentes (principio abierto/cerrado; ejercicio en clase S2 con `REGISTRO_CONSULAR`).
- `GET /api/tarifas` según el contrato. Tabla `tramites.tarifa_base`.

### Módulo `notificaciones`

- API pública del módulo: `NotificacionesApi.enviar(AvisoCommand)`.
- Puerto `CanalDeEnvioPort` con adaptadores `ConsolaCanalAdapter` y `CorreoCanalAdapter` (JavaMailSender hacia Mailpit). Selección por `notificaciones.canal` (`@ConditionalOnProperty`) — ejemplo de patrón Adapter y de cambio de infraestructura sin tocar el dominio (S2).
- `notificaciones.simular-falla=true` → `NotificacionFallidaException`.
- Persistencia del aviso en `notificaciones.aviso`.

### Comportamientos clave

1. **Reserva síncrona y acoplada:** `RegistrarCitaService` (transaccional) reserva el cupo con bloqueo pesimista, guarda la cita y llama a `NotificacionPort` dentro de la misma transacción. Si la notificación falla, toda la reserva se revierte (`500`). Documentar en Javadoc: "Acoplamiento deliberado para la comparación de estilos (1.2)".
2. **Evento de dominio:** además se publica `CitaReservada` con `ApplicationEventPublisher` y existe un `@ApplicationModuleListener` de ejemplo en notificaciones que solo registra una línea de log ("Observer", 2.1). No reemplaza la llamada síncrona.
3. **Modo CAP** (`consistencia.modo`): implementar exactamente la regla 6 del contrato. `DisponibilidadCaffeineAdapter` refresca cada 10 s la disponibilidad de los próximos 7 días por oficina; `ConsultarDisponibilidadService` decide según el modo. Excepción `BaseDeDatosNoDisponibleException` → `503`.
4. **`GET /api/sistema/info`** → `variante: MONOLITO_MODULAR`, `arquitecturaInterna: HEXAGONAL`, modo actual.
5. **Generador de cupos** al iniciar (idempotente) y migraciones Flyway por esquema (`db/migration/citas`, `.../tramites`, `.../notificaciones`) con datos semilla del contrato.
6. **Configuración:** `application.yml` (Docker) y `application-local.yml` (Eclipse, modo híbrido) según la regla 02.

### Pruebas

- Unitarias: `ReglasDeReservaTest`, `CitaTest`, `CalculadoraDeTarifasTest`, `ConsultarDisponibilidadServiceTest` (CP vs. AP con puerto simulado).
- `ReglasDeArquitecturaTest` (ArchUnit, nombres en español, comentario "cómo romperla en clase").
- `ModulosTest` (Spring Modulith `verify()` + documentación).
- Integración (`@Tag("integracion")`, Testcontainers): reservar, duplicado (`409`), sin capacidad (`409`), notificación fallida revierte la reserva, consulta por DNI.

### Docker

`m1-estilos/monolito-modular/Dockerfile` según la regla 03 (puerto 8081). El compose del módulo se crea en el prompt 07; por ahora agrega un `docker-compose.dev.yml` temporal mínimo (postgres + mailpit + monolito) para verificar, que el prompt 07 reemplazará.

### README del proyecto

`m1-estilos/monolito-modular/README.md`: qué ilustra, mapa de paquetes, cómo ejecutar en Docker y en Eclipse (modo híbrido), propiedades de simulación, endpoints y "Verificación rápida" con curl.

## Criterios de aceptación

- `mvn -B verify` en contenedor: todas las pruebas pasan.
- Con el compose temporal: `POST /api/citas` con el ejemplo del contrato devuelve `201`; segunda reserva igual → `409 cita-duplicada`.
- Con `NOTIFICACIONES_SIMULAR_FALLA=true` la reserva devuelve `500` y la cita no queda en la base.
- Con `CONSISTENCIA_MODO=AP` y `docker network disconnect` de la red de datos, `GET /api/oficinas/MAD/disponibilidad?fecha=…` responde `200` con `origen: CACHE`; con `CP` responde `503` en menos de 3 s.
- `GET /api/tarifas?tipoTramite=APOSTILLA&pais=ES` → `33.00` (30 + 10 %); `tipoTramite=REGISTRO_CONSULAR` → `422`/`404` con `ProblemDetail` claro.
- Swagger UI disponible en `http://localhost:8081/swagger-ui.html`.
- No existe nada de reprogramación (A1).

## Commit

`feat(m1): monolito modular hexagonal`
