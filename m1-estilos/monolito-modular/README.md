# Monolito Modular Hexagonal (`consular-monolito`)

Servicio monolítico modular con arquitectura hexagonal de la **Plataforma Digital de Servicios Consulares y de Legalización** (UTEC Posgrado – MRREE).

Constituye la implementación de referencia para las **Sesiones S1 y S2** del curso:
- **Atributos de calidad (1.1):** Disponibilidad, resiliencia y control de concurrencia pesimista en cupos.
- **Comparativa de estilos arquitectónicos (1.2):** Variante "Monolito" frente a microservicios y serverless.
- **Arquitectura Hexagonal (1.3):** Separación rigurosa de dominio puro, puertos primarios/secundarios y adaptadores.
- **Teorema CAP / PACELC (1.4):** Simulación de consistencia fuerte (`CP`) vs. alta disponibilidad con caché (`AP`).
- **Patrones de diseño (2.1):**
  - **Strategy:** Cálculo extensible de tarifas en `tramites` (`TarifaStrategy`, principio OCP).
  - **Observer:** Publicación de evento `CitaReservada` mediante `ApplicationEventPublisher` y consumo desacoplado en notificaciones.
  - **Adapter:** Sustitución transparente del canal de notificaciones (`CONSOLA` vs. `CORREO`).

---

## 1. Mapa de Paquetes y Módulos (Spring Modulith)

```text
pe.utec.consular.monolito
├── MonolitoApplication.java             // Punto de entrada Spring Boot y configuración CORS
│
├── citas/                               // Módulo de reservas, cupos, oficinas y modo CAP
│   ├── domain/                          // Cita, Cupo, OficinaConsular, CodigoCita, EstadoCita, CitaReservada, ReglasDeReserva
│   ├── application/
│   │   ├── port/in/                     // RegistrarCitaUseCase (+Command), ConsultarCitaUseCase, CancelarCitaUseCase,
│   │   │                                // ConsultarDisponibilidadUseCase, ListarOficinasUseCase
│   │   ├── port/out/                    // CitaRepositoryPort, CupoRepositoryPort, OficinaRepositoryPort, NotificacionPort, DisponibilidadCachePort
│   │   └── service/                     // RegistrarCitaService, ConsultarCitaService, CancelarCitaService,
│   │                                    // ConsultarDisponibilidadService, ListarOficinasService, GeneradorDeCupos
│   └── adapter/
│       ├── in/web/                      // CitaController, OficinaController, SistemaController, DTOs (records), CitaWebMapper
│       ├── out/persistence/             // CitaJpaEntity, CupoJpaEntity, OficinaJpaEntity, repositorios Spring Data y Adapters JPA
│       ├── out/notificaciones/          // NotificacionModuloAdapter (acoplamiento deliberado hacia NotificacionesApi)
│       └── out/cache/                   // DisponibilidadCaffeineAdapter (caché Caffeine con refresco cada 10 s)
│
├── tramites/                            // Módulo de tarifas (patrón Strategy)
│   ├── domain/tarifa/                   // TarifaStrategy, LegalizacionTarifaStrategy, ApostillaTarifaStrategy, CalculadoraDeTarifas
│   ├── application/                     // CalcularTarifaUseCase, CalcularTarifaService, TarifaBaseRepositoryPort
│   └── adapter/                         // TarifaController, TarifaBaseJpaEntity, TarifaBaseJpaAdapter
│
├── notificaciones/                      // Módulo de avisos y patrón Observer
│   ├── NotificacionesApi.java           // API pública de comunicación inter-módulos
│   ├── AvisoCommand.java                // DTO público del comando de aviso
│   ├── domain/                          // Aviso, CanalNotificacion, EstadoNotificacion
│   ├── application/                     // NotificacionesService, CanalDeEnvioPort, AvisoRepositoryPort
│   └── adapter/
│       ├── in/events/                   // CitaReservadaObserver (@ApplicationModuleListener)
│       ├── out/canal/                   // ConsolaCanalAdapter, CorreoCanalAdapter (JavaMailSender hacia Mailpit)
│       └── out/persistence/             // AvisoJpaEntity, AvisoJpaAdapter
│
└── compartido/                          // Tipos comunes (Spring Modulith OPEN)
    ├── domain/                          // Dni, Email (Value Objects autovalidados), ModoConsistencia, TipoTramite
    ├── infraestructura/                 // ConsistenciaProperties, NotificacionesProperties, FlywayMultiSchemaConfig
    └── web/                             // ManejadorGlobalErrores (@RestControllerAdvice, ProblemDetail RFC 9457)
```

---

## 2. Propiedades de Simulación Pedagógica

| Propiedad | Variable de Entorno | Valores Permitidos | Descripción |
| --- | --- | --- | --- |
| `consistencia.modo` | `CONSISTENCIA_MODO` | `CP` (defecto), `AP` | Teorema CAP: `CP` exige BD (falla con 503 ante partición en <3s); `AP` responde desde caché Caffeine (`origen: CACHE`). |
| `notificaciones.canal` | `NOTIFICACIONES_CANAL` | `CONSOLA` (defecto), `CORREO` | Patrón Adapter: selecciona el canal de transporte de avisos (logs o Mailpit SMTP :1025). |
| `notificaciones.simular-falla` | `NOTIFICACIONES_SIMULAR_FALLA` | `false` (defecto), `true` | Simula falla síncrona en notificación: al estar dentro de `@Transactional`, revierte la reserva (`500`). |

---

## 3. Ejecución

### Opción A: Con Docker Compose (Entorno Completo)

Desde la carpeta `m1-estilos`:

```powershell
# Levantar el entorno (Postgres + Mailpit + Frontend + Monolito)
.\scripts\levantar.ps1 monolito
# O directamente con Docker Compose:
docker compose --profile monolito up -d --build

# Verificar que los servicios estén healthy y operativos
docker compose ps

# Ver logs en vivo
docker compose logs -f monolito

# Detener y reiniciar volúmenes
.\scripts\limpiar.ps1 -Forzar
```

### Opción B: Modo Híbrido en Eclipse (Desarrollo y Depuración)

1. Inicie únicamente la base de datos y Mailpit desde `m1-estilos`:
   ```powershell
   docker compose up -d postgres mailpit
   ```
2. Abra Eclipse / Spring Tools Suite e importe `m1-estilos/monolito-modular` como proyecto Maven existente.
3. Haga clic derecho sobre `MonolitoApplication.java` -> *Run As > Spring Boot App*.
4. Asegúrese de activar el perfil `local` en *Run Configurations > Spring Boot > Active Profiles: local* (utilizará `application-local.yml` apuntando a `localhost:5432`).

---

## 4. Endpoints Principales

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/api/oficinas` | Catálogo de oficinas consulares |
| `GET` | `/api/oficinas/{oficinaId}/disponibilidad?fecha=YYYY-MM-DD` | Disponibilidad y cupos libres |
| `POST` | `/api/citas` | Reserva una cita (bloqueo pesimista + notificación síncrona) |
| `GET` | `/api/citas/{citaId}` | Consulta de cita por identificador UUID |
| `GET` | `/api/citas?dni={dni}` | Listado de citas de un ciudadano por DNI |
| `DELETE` | `/api/citas/{citaId}` | Cancelación de cita y liberación de cupo |
| `GET` | `/api/tarifas?tipoTramite=&pais=` | Cálculo de tarifa consular mediante patrón Strategy |
| `GET` | `/api/sistema/info` | Variante arquitectónica (`MONOLITO_MODULAR`), estilo (`HEXAGONAL`) y modo CAP |
| `GET` | `/swagger-ui.html` | Documentación interactiva Swagger UI |
| `GET` | `/v3/api-docs` | Especificación OpenAPI 3 en formato JSON |

---

## 5. Verificación Rápida con cURL

```powershell
# 1. Información del sistema
curl -i http://localhost:8081/api/sistema/info

# 2. Catálogo de oficinas
curl -i http://localhost:8081/api/oficinas

# 3. Consultar disponibilidad en Madrid para una fecha válida
curl -i "http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-30"

# 4. Registrar una cita (ejemplo del contrato global)
curl -i -X POST http://localhost:8081/api/citas `
  -H "Content-Type: application/json" `
  -d '{\"dni\":\"40000001\",\"nombres\":\"Ana Pérez\",\"email\":\"ana@correo.lab\",\"oficinaId\":\"MAD\",\"fecha\":\"2026-11-30\",\"hora\":\"08:00\",\"tipoTramite\":\"LEGALIZACION\"}'

# 5. Intentar duplicar la misma cita (debe responder 409 cita-duplicada)
curl -i -X POST http://localhost:8081/api/citas `
  -H "Content-Type: application/json" `
  -d '{\"dni\":\"40000001\",\"nombres\":\"Ana Pérez\",\"email\":\"ana@correo.lab\",\"oficinaId\":\"MAD\",\"fecha\":\"2026-11-30\",\"hora\":\"08:00\",\"tipoTramite\":\"LEGALIZACION\"}'

# 6. Consultar citas por DNI
curl -i "http://localhost:8081/api/citas?dni=40000001"

# 7. Cálculo de tarifas (patrón Strategy)
# Fuera de Sudamérica (ES): 30.00 + 10% = 33.00 PEN
curl -i "http://localhost:8081/api/tarifas?tipoTramite=APOSTILLA&pais=ES"

# Trámite no implementado (debe responder 422 tarifa-no-disponible)
curl -i "http://localhost:8081/api/tarifas?tipoTramite=REGISTRO_CONSULAR&pais=ES"
```
