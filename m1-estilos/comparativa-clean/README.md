# Comparativa Clean Architecture (`consular-clean`)

Implementación con **Clean Architecture** (Robert C. Martin) del módulo de **Citas** y **Tarifas** de la **Plataforma Digital de Servicios Consulares y de Legalización** (UTEC Posgrado – MRREE).

Diseñada para la **Sesión S2** (sección 1.3 del plan): el participante abre en Eclipse este proyecto (`consular-clean`) lado a lado con `monolito-modular` (`consular-monolito`) para comparar:
- Los **4 círculos concéntricos** (`entities` → `usecase` → `interfaceadapters` → `frameworks`) frente a **Puertos y Adaptadores (Hexagonal)**.
- La **ausencia de anotaciones de Spring** en `entities` y `usecase` (ensamblado explícito con `@Bean` en `frameworks.config`).
- El flujo **Output Boundary → Presenter → ViewModel** (`CitaPresenter`) frente al retorno directo de la entidad desde el servicio de aplicación hexagonal.

---

## 1. Estructura de Paquetes (Círculos Concéntricos)

```text
pe.utec.consular.clean
├── CleanApplication.java
├── citas/
│   ├── entities/                     // Círculo 1: Reglas empresariales (Cita, Cupo, OficinaConsular, ReglasDeReserva)
│   ├── usecase/                      // Círculo 2: Casos de uso (InputBoundary, Interactor, OutputBoundary, Gateways)
│   │   ├── registrar/                // RegistrarCitaInputBoundary, RegistrarCitaInteractor, RegistrarCitaOutputBoundary,
│   │   │                             // RegistrarCitaRequestModel, RegistrarCitaResponseModel
│   │   ├── consultar/                // ConsultarCitaInputBoundary, ConsultarCitaInteractor, ConsultarCitaOutputBoundary
│   │   ├── cancelar/                 // CancelarCitaInputBoundary, CancelarCitaInteractor, CancelarCitaOutputBoundary
│   │   ├── disponibilidad/           // ConsultarDisponibilidadInputBoundary, ConsultarDisponibilidadInteractor, ...
│   │   ├── oficinas/                 // ListarOficinasInputBoundary, ListarOficinasInteractor, ...
│   │   └── CitaGateway, CupoGateway, OficinaGateway, NotificadorGateway, DisponibilidadCacheGateway
│   ├── interfaceadapters/            // Círculo 3: Controladores, Presentadores y Gateways
│   │   ├── controller/               // CitaController, OficinaController, SistemaController, RegistrarCitaHttpRequest
│   │   ├── presenter/                // CitaPresenter -> CitaViewModel, DisponibilidadPresenter, OficinaPresenter
│   │   └── gateway/                  // CitaJpaGateway, CupoJpaGateway, OficinaJpaGateway, NotificadorConsolaGateway
│   └── frameworks/                   // Círculo 4: Spring Boot, JPA, Web y ensamblado @Bean
│       ├── persistence/              // CitaJpaEntity, CupoJpaEntity, OficinaJpaEntity y repositorios Spring Data
│       ├── web/                      // ManejadorGlobalErrores (ProblemDetail RFC 9457)
│       └── config/                   // CleanArchitectureBeansConfig (@Bean), FlywayMultiSchemaConfig
└── tarifas/                          // Módulo reducido con Patrón Strategy (LegalizacionTarifaStrategy, ApostillaTarifaStrategy)
```

---

## 2. Ejecución en Modo Híbrido desde Eclipse

1. Levante PostgreSQL desde `m1-estilos`:
   ```powershell
   docker compose -f docker-compose.dev.yml up -d postgres
   ```
2. En Eclipse / Spring Tools Suite, importe `m1-estilos/comparativa-clean` como *Existing Maven Project*.
3. Ejecute `pe.utec.consular.clean.CleanApplication` (*Run As → Spring Boot App*) con el perfil activo `local`.
4. Verifique en `http://localhost:8081/api/sistema/info` que responde:
   ```json
   {
     "variante": "MONOLITO_MODULAR",
     "arquitecturaInterna": "CLEAN",
     "modoConsistencia": "CP",
     "version": "1.0.0"
   }
   ```

---

## 3. Verificación con cURL (Misma API que el Monolito)

```powershell
# 1. Información del sistema (arquitecturaInterna: CLEAN)
curl -i http://localhost:8081/api/sistema/info

# 2. Catálogo de oficinas
curl -i http://localhost:8081/api/oficinas

# 3. Consultar disponibilidad en Madrid
curl -i "http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-30"

# 4. Registrar una cita
curl -i -X POST http://localhost:8081/api/citas `
  -H "Content-Type: application/json" `
  -d '{\"dni\":\"40000001\",\"nombres\":\"Ana Pérez\",\"email\":\"ana@correo.lab\",\"oficinaId\":\"MAD\",\"fecha\":\"2026-11-30\",\"hora\":\"08:00\",\"tipoTramite\":\"LEGALIZACION\"}'

# 5. Intentar duplicar la misma cita (409 cita-duplicada)
curl -i -X POST http://localhost:8081/api/citas `
  -H "Content-Type: application/json" `
  -d '{\"dni\":\"40000001\",\"nombres\":\"Ana Pérez\",\"email\":\"ana@correo.lab\",\"oficinaId\":\"MAD\",\"fecha\":\"2026-11-30\",\"hora\":\"08:00\",\"tipoTramite\":\"LEGALIZACION\"}'

# 6. Consultar citas por DNI
curl -i "http://localhost:8081/api/citas?dni=40000001"

# 7. Cálculo de tarifas (Strategy)
curl -i "http://localhost:8081/api/tarifas?tipoTramite=APOSTILLA&pais=ES"
curl -i "http://localhost:8081/api/tarifas?tipoTramite=REGISTRO_CONSULAR&pais=ES"
```

