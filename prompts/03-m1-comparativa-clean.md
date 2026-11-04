# Prompt 03 — M1: Comparativa Clean Architecture

> Rama `main`. Referencias: contrato (secciones 4–5), regla 01 (sección Clean Architecture).

## Objetivo didáctico

Implementar el **mismo módulo Citas** del monolito con Clean Architecture para que en S2 el participante abra ambos proyectos lado a lado en Eclipse y complete la tabla de equivalencias (plan, sección 7, 1.3). La API y el comportamiento observable deben ser **idénticos** a los del monolito (solo citas, oficinas, disponibilidad con modo CAP, tarifas y sistema/info).

## Alcance

Proyecto `m1-estilos/comparativa-clean` (artifactId `consular-clean`, principal `pe.utec.consular.clean.CleanApplication`), puerto 8081, mismo esquema `citas` de la base de datos.

```text
pe.utec.consular.clean
├── citas/
│   ├── entities/            Cita, Cupo, OficinaConsular, ReglasDeReserva (sin dependencias externas)
│   ├── usecase/
│   │   ├── registrar/       RegistrarCitaInputBoundary, RegistrarCitaInteractor,
│   │   │                    RegistrarCitaOutputBoundary, RegistrarCitaRequestModel, RegistrarCitaResponseModel
│   │   ├── consultar/       … (misma estructura para consultar, cancelar, disponibilidad, oficinas)
│   │   └── CitaGateway, CupoGateway, OficinaGateway, NotificadorGateway
│   ├── interfaceadapters/
│   │   ├── controller/      CitaController (construye RequestModel, invoca InputBoundary)
│   │   ├── presenter/       CitaPresenter implementa OutputBoundary → CitaViewModel (JSON)
│   │   └── gateway/         CitaJpaGateway, CupoJpaGateway, OficinaJpaGateway, NotificadorConsolaGateway
│   └── frameworks/
│       ├── persistence/     Entidades JPA y repositorios Spring Data
│       ├── web/             Configuración web, manejo de errores (ProblemDetail)
│       └── config/          Ensamblado de beans (aquí se conectan Interactor, Presenter y Gateways)
└── tarifas/ (misma organización, reducida: Strategy en entities o usecase)
```

### Reglas

- Las clases de `entities` y `usecase` **no** usan anotaciones de Spring; el ensamblado se hace en `frameworks.config` con métodos `@Bean`. Esto hace visible la diferencia con el monolito hexagonal, donde los servicios sí llevan `@Service`.
- El presentador se usa de verdad: el controlador obtiene la respuesta del `CitaPresenter` (patrón Output Boundary → Presenter → ViewModel). Documentar en Javadoc la diferencia con la versión hexagonal (que devuelve el resultado directamente desde el caso de uso).
- Las migraciones Flyway y los datos semilla son los mismos que en el monolito (copiar los scripts de `citas` y `tramites`; no compartir archivos entre proyectos para que cada uno sea autosuficiente).
- `GET /api/sistema/info` → `arquitecturaInterna: CLEAN`, `variante: MONOLITO_MODULAR`.
- Notificación: solo canal consola (suficiente para la comparación).

### Pruebas

- `ReglasDeArquitecturaTest` con la regla de dependencias concéntrica: `entities` no depende de nadie; `usecase` solo de `entities`; `interfaceadapters` de `usecase` y `entities`; `frameworks` de todos. Usar `Architectures.onionArchitecture()` o reglas por capas de ArchUnit, con nombres en español.
- Unitarias del `RegistrarCitaInteractor` con dobles de prueba de los gateways y del presentador.
- Integración: los mismos casos que en el monolito para citas.

### Documento comparativo

Crear `docs/matrices/m1-hexagonal-vs-clean.md` con la tabla de equivalencias **con rutas reales** de ambos proyectos (rol → clase hexagonal → clase clean) y las preguntas guía de S2.

## Criterios de aceptación

- `mvn -B verify` pasa para `comparativa-clean`.
- Con un compose temporal, las mismas peticiones curl del README del monolito producen las mismas respuestas (salvo `arquitecturaInterna`).
- Romper la regla (importar `jakarta.persistence.Entity` en `entities.Cita`) hace fallar `ReglasDeArquitecturaTest` con un mensaje en español comprensible. Revertir después.
- `docs/matrices/m1-hexagonal-vs-clean.md` existe y las rutas son correctas.

## Commit

`feat(m1): comparativa clean architecture`
