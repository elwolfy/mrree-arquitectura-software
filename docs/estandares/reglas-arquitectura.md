# Reglas de Arquitectura y Verificación Automatizada con ArchUnit

- **Sección del Plan:** 1.3 (Arquitectura Hexagonal vs. Clean Architecture) y 4.4 (Gobernanza Técnica y Fitness Functions).
- **Ejecución en CI:** Todas las reglas se ejecutan automáticamente en la fase `mvn test` / `mvn verify` de cada módulo. **Nunca se debe desactivar (`@Disabled`) una prueba de arquitectura para forzar el pase de un build.**

---

## 1. Reglas de Arquitectura Hexagonal (Regla General de Servicios)

En todos los servicios hexagonales (`monolito-modular`, `citas-service`, `tramites-service`, `pagos-service`, `emision-service`, `identidad-service`, `notificaciones-service`), la estructura de paquetes bajo `pe.utec.consular.<contexto>` cumple seis funciones de aptitud (*architectural fitness functions*) verificadas por `ReglasDeArquitecturaTest`:

| N.° | Regla ArchUnit (`@ArchTest`) | Condición Verificada | Propósito Arquitectónico |
| --- | --- | --- | --- |
| **1** | `dominio_no_depende_de_spring_ni_adaptadores` | Las clases en `..domain..` solo pueden depender de `java..` y `..domain..`. | Garantiza que las reglas de negocio, agregados, eventos y objetos de valor sean Java puro, libres de Spring, Hibernate/JPA, Jackson, RabbitMQ o gRPC. |
| **2** | `aplicacion_no_depende_de_adaptadores` | Ninguna clase en `..application..` puede depender de `..adapter..`. | Obliga a que los casos de uso y orquestadores (como `SagaSolicitudTramite`) se comuniquen con la infraestructura exclusivamente mediante interfaces de puertos (`..port.out..`). |
| **3** | `convencion_nombres_puertos_entrada` | Las clases en `..port.in..` deben terminar en `UseCase` o `Command`. | Estandariza el catálogo de casos de uso primarios del bounded context. |
| **4** | `convencion_nombres_puertos_salida` | Las interfaces en `..port.out..` deben terminar en `Port`. | Hace explícitos los contratos requeridos hacia bases de datos, brokers, cachés, clientes HTTP o métricas. |
| **5** | `controladores_deben_residir_en_adapter_web` | Toda clase anotada con `@RestController` o `@Controller` debe residir en `..adapter.in.web..` y terminar en `Controller`. | Impide que se expongan endpoints HTTP desde la capa de aplicación o de dominio. |
| **6** | `controladores_no_exponen_entidades_jpa` | Ninguna clase en `..adapter.in.web..` puede depender de `..adapter.out.persistence..`. | Evita la fuga del modelo relacional (`*JpaEntity`) hacia los contratos JSON públicos; exige el uso de DTOs (`record`). |

---

## 2. Ubicación de las Pruebas de Arquitectura por Módulo

### Módulo 1 (`m1-estilos`)
- **Monolito Modular (Hexagonal + Spring Modulith):**
  - [m1-estilos/monolito-modular/src/test/java/pe/utec/consular/monolito/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m1-estilos/monolito-modular/src/test/java/pe/utec/consular/monolito/ReglasDeArquitecturaTest.java)
  - [m1-estilos/monolito-modular/src/test/java/pe/utec/consular/monolito/ModulosTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m1-estilos/monolito-modular/src/test/java/pe/utec/consular/monolito/ModulosTest.java) (`ApplicationModules.of(MonolitoApplication.class).verify()`)
- **Comparativa Clean Architecture (`entities <- usecase <- interfaceadapters <- frameworks`):**
  - [m1-estilos/comparativa-clean/src/test/java/pe/utec/consular/clean/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m1-estilos/comparativa-clean/src/test/java/pe/utec/consular/clean/ReglasDeArquitecturaTest.java)
- **Microservicios M1:**
  - [m1-estilos/microservicios/citas-service/src/test/java/pe/utec/consular/citas/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m1-estilos/microservicios/citas-service/src/test/java/pe/utec/consular/citas/ReglasDeArquitecturaTest.java)
  - [m1-estilos/microservicios/notificaciones-service/src/test/java/pe/utec/consular/notificaciones/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m1-estilos/microservicios/notificaciones-service/src/test/java/pe/utec/consular/notificaciones/ReglasDeArquitecturaTest.java)

### Módulo 2 (`m2-integracion`)
- [m2-integracion/citas-service/src/test/java/pe/utec/consular/citas/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m2-integracion/citas-service/src/test/java/pe/utec/consular/citas/ReglasDeArquitecturaTest.java)
- [m2-integracion/tramites-service/src/test/java/pe/utec/consular/tramites/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m2-integracion/tramites-service/src/test/java/pe/utec/consular/tramites/ReglasDeArquitecturaTest.java)
- [m2-integracion/identidad-service/src/test/java/pe/utec/consular/identidad/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m2-integracion/identidad-service/src/test/java/pe/utec/consular/identidad/ReglasDeArquitecturaTest.java)

### Módulo 3 (`m3-datos`)
- [m3-datos/citas-service/src/test/java/pe/utec/consular/citas/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m3-datos/citas-service/src/test/java/pe/utec/consular/citas/ReglasDeArquitecturaTest.java)
- [m3-datos/tramites-service/src/test/java/pe/utec/consular/tramites/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m3-datos/tramites-service/src/test/java/pe/utec/consular/tramites/ReglasDeArquitecturaTest.java)
- [m3-datos/identidad-service/src/test/java/pe/utec/consular/identidad/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m3-datos/identidad-service/src/test/java/pe/utec/consular/identidad/ReglasDeArquitecturaTest.java)

### Módulo 4 (`m4-observabilidad`)
- [m4-observabilidad/citas-service/src/test/java/pe/utec/consular/citas/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m4-observabilidad/citas-service/src/test/java/pe/utec/consular/citas/ReglasDeArquitecturaTest.java)
- [m4-observabilidad/tramites-service/src/test/java/pe/utec/consular/tramites/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m4-observabilidad/tramites-service/src/test/java/pe/utec/consular/tramites/ReglasDeArquitecturaTest.java)
- [m4-observabilidad/identidad-service/src/test/java/pe/utec/consular/identidad/ReglasDeArquitecturaTest.java](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/m4-observabilidad/identidad-service/src/test/java/pe/utec/consular/identidad/ReglasDeArquitecturaTest.java)

